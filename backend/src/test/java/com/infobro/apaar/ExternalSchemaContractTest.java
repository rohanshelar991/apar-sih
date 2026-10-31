package com.infobro.apaar;

import jakarta.persistence.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_DB_TESTS", matches = "true")
@SpringBootTest
@ActiveProfiles("postgres")
class ExternalSchemaContractTest {
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired DataSource dataSource;
    @Autowired Flyway flyway;

    @Test void everyEntityColumnKeyRelationshipAndDefaultMatchesLiveSchema() throws Exception {
        assertThat(entityManagerFactory.getMetamodel().getEntities()).hasSize(13);
        try (Connection connection = dataSource.getConnection()) {
            for (var entity : entityManagerFactory.getMetamodel().getEntities()) {
                Class<?> type = entity.getJavaType();
                String table = type.getAnnotation(Table.class).name();
                Map<String, Map<String, String>> actual = new HashMap<>();
                try (PreparedStatement s = connection.prepareStatement("select column_name,udt_name,character_maximum_length,is_nullable,is_identity,identity_generation,column_default from information_schema.columns where table_schema='public' and table_name=?")) {
                    s.setString(1, table);
                    try (ResultSet rs = s.executeQuery()) {
                        while (rs.next()) {
                            Map<String, String> col = new HashMap<>();
                            for (String name : List.of("udt_name", "character_maximum_length", "is_nullable", "is_identity", "identity_generation", "column_default")) col.put(name, rs.getString(name));
                            actual.put(rs.getString("column_name"), col);
                        }
                    }
                }
                Set<String> expectedColumns = new HashSet<>();
                for (var field : type.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(Transient.class)) continue;
                    Column column = field.getAnnotation(Column.class);
                    JoinColumn join = field.getAnnotation(JoinColumn.class);
                    boolean id = field.isAnnotationPresent(Id.class);
                    String name = join != null ? join.name() : column != null && !column.name().isEmpty() ? column.name()
                        : field.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
                    expectedColumns.add(name);
                    assertThat(actual).as(table).containsKey(name);
                    var live = actual.get(name);
                    Class<?> javaType = field.getType();
                    String dbType = join != null || javaType == Long.class ? "int8" : javaType == Integer.class ? "int4"
                        : javaType == Boolean.class ? "bool" : javaType == LocalDate.class ? "date"
                        : javaType == LocalDateTime.class ? "timestamp" : "varchar";
                    assertThat(live.get("udt_name")).as(table + "." + name).isEqualTo(dbType);
                    if (javaType == String.class) assertThat(live.get("character_maximum_length"))
                        .as(table + "." + name + " length").isEqualTo(String.valueOf(column != null ? column.length() : 255));
                    boolean nullable = !id && (join != null ? join.nullable() : column == null || column.nullable());
                    assertThat(live.get("is_nullable")).as(table + "." + name + " nullable").isEqualTo(nullable ? "YES" : "NO");
                    // Existing defaults are Java initializers, not database DEFAULT expressions.
                    assertThat(live.get("column_default")).as(table + "." + name + " default").isNull();
                    if (id) {
                        assertThat(field.getAnnotation(GeneratedValue.class).strategy()).isEqualTo(GenerationType.IDENTITY);
                        assertThat(live.get("is_identity")).isEqualTo("YES");
                        assertThat(live.get("identity_generation")).isEqualTo("BY DEFAULT");
                        try (ResultSet rs = connection.getMetaData().getPrimaryKeys(null, "public", table)) {
                            assertThat(rs.next()).isTrue(); assertThat(rs.getString("COLUMN_NAME")).isEqualTo(name);
                            assertThat(rs.next()).isFalse();
                        }
                    }
                    if (join != null) {
                        String parent = javaType.getAnnotation(Table.class).name();
                        boolean found = false;
                        try (ResultSet keys = connection.getMetaData().getImportedKeys(null, "public", table)) {
                            while (keys.next()) if (name.equals(keys.getString("FKCOLUMN_NAME"))) {
                                assertThat(keys.getString("PKTABLE_NAME")).isEqualTo(parent);
                                assertThat(keys.getString("PKCOLUMN_NAME")).isEqualTo("id"); found = true;
                            }
                        }
                        assertThat(found).as(table + "." + name + " foreign key").isTrue();
                    }
                    if (column != null && column.unique()) {
                        boolean found = false;
                        try (ResultSet indexes = connection.getMetaData().getIndexInfo(null, "public", table, true, false)) {
                            while (indexes.next()) if (name.equals(indexes.getString("COLUMN_NAME"))) found = true;
                        }
                        assertThat(found).as(table + "." + name + " unique").isTrue();
                    }
                }
                assertThat(actual.keySet()).as(table + " exact columns").isEqualTo(expectedColumns);
            }
        }
    }

    @Test void declaredMigrationIndexesAndChecksExist() throws Exception {
        String v2 = Files.readString(Path.of("src/main/resources/db/migration/V2__indexes_and_constraints.sql"));
        var indexes = Pattern.compile("CREATE (?:UNIQUE )?INDEX (\\w+) ON (\\w+)").matcher(v2);
        try (Connection connection = dataSource.getConnection()) {
            while (indexes.find()) {
                Set<String> names = new HashSet<>();
                try (ResultSet rs = connection.getMetaData().getIndexInfo(null, "public", indexes.group(2), false, false)) {
                    while (rs.next()) names.add(rs.getString("INDEX_NAME"));
                }
                assertThat(names).contains(indexes.group(1));
            }
            String v1 = Files.readString(Path.of("src/main/resources/db/migration/V1__baseline_schema.sql"));
            var checks = Pattern.compile("CONSTRAINT (ck_\\w+)").matcher(v1);
            Set<String> expected = new HashSet<>(), live = new HashSet<>();
            while (checks.find()) expected.add(checks.group(1));
            try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("select conname from pg_constraint where connamespace='public'::regnamespace and contype='c'")) {
                while (rs.next()) live.add(rs.getString(1));
            }
            assertThat(live).containsAll(expected);
        }
    }

    @Test void migrationHistoryAndRolePrivilegesAreSeparated() throws Exception {
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        try (Connection migration = flyway.getConfiguration().getDataSource().getConnection(); Statement s = migration.createStatement()) {
            migration.setAutoCommit(false);
            try {
                try (ResultSet rs = s.executeQuery("select current_user")) { rs.next(); assertThat(rs.getString(1)).isEqualTo("apaar_migrator"); }
                try (ResultSet rs = s.executeQuery("select version,success,installed_by,checksum from public.flyway_schema_history order by installed_rank")) {
                    for (String version : List.of("1", "2")) {
                        assertThat(rs.next()).isTrue(); assertThat(rs.getString(1)).isEqualTo(version);
                        assertThat(rs.getBoolean(2)).isTrue(); assertThat(rs.getString(3)).isEqualTo("apaar_migrator");
                        assertThat(rs.getObject(4)).isNotNull();
                    }
                    assertThat(rs.next()).isFalse();
                }
                String probe = "migration_probe_" + UUID.randomUUID().toString().replace("-", "");
                s.execute("CREATE TABLE public." + probe + " (id bigint)");
                s.execute("ALTER TABLE public." + probe + " ADD COLUMN note varchar(20)");
            } finally { migration.rollback(); }
        }
        try (Connection app = dataSource.getConnection(); Statement s = app.createStatement()) {
            assertThatThrownBy(() -> s.execute("SELECT installed_rank FROM public.flyway_schema_history"))
                .isInstanceOf(SQLException.class).extracting(e -> ((SQLException)e).getSQLState()).isEqualTo("42501");
            try (ResultSet rs = s.executeQuery("select rolsuper,rolcreatedb,rolcreaterole from pg_roles where rolname=current_user")) {
                rs.next(); for (int i=1; i<=3; i++) assertThat(rs.getBoolean(i)).isFalse();
            }
            try (PreparedStatement privileges = app.prepareStatement("select has_table_privilege(current_user, ?, ?)")) {
                for (var entity : entityManagerFactory.getMetamodel().getEntities()) {
                    String table = entity.getJavaType().getAnnotation(Table.class).name();
                    for (String privilege : List.of("SELECT", "INSERT", "UPDATE", "DELETE")) {
                        privileges.setString(1, "public." + table); privileges.setString(2, privilege);
                        try (ResultSet rs = privileges.executeQuery()) {
                            rs.next();
                            boolean expected = !(List.of("audit_logs", "student_history").contains(table)
                                && List.of("UPDATE", "DELETE").contains(privilege));
                            assertThat(rs.getBoolean(1)).as(table + " " + privilege).isEqualTo(expected);
                        }
                    }
                }
            }
        }
    }

    @Test void foreignKeysRejectOrphansWithoutRetainingRows() throws Exception {
        try (Connection app = dataSource.getConnection(); PreparedStatement s = app.prepareStatement(
            "insert into programmes(institution_id,name,code,academic_year,approved_intake,supernumerary_quota) values (?,?,?,?,?,?)")) {
            app.setAutoCommit(false);
            try {
                s.setLong(1, -1); s.setString(2, "FK probe"); s.setString(3, "FK");
                s.setInt(4, 2026); s.setInt(5, 1); s.setInt(6, 0);
                assertThatThrownBy(s::executeUpdate).isInstanceOf(SQLException.class)
                    .extracting(e -> ((SQLException)e).getSQLState()).isEqualTo("23503");
            } finally { app.rollback(); }
        }
    }
}
