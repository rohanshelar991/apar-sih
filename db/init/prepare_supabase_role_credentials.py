"""Offline credential preparation only. No database/network client or SQL execution.

Outputs user-private passwords and a SQL copy containing SCRAM verifiers, never
plaintext passwords. The manual project-confirmation gate remains unfilled.
"""
import base64
import csv
import hashlib
import hmac
import io
import json
import os
from pathlib import Path
import secrets
import subprocess

PROJECT = 'hxmlezblzvdbacebsmcb'
ITERATIONS = 16384


def b64(value):
    return base64.b64encode(value).decode('ascii')


def verifier(password):
    salt = secrets.token_bytes(16)
    salted = hashlib.pbkdf2_hmac('sha256', password.encode('ascii'), salt, ITERATIONS)
    stored = hashlib.sha256(hmac.digest(salted, b'Client Key', 'sha256')).digest()
    server = hmac.digest(salted, b'Server Key', 'sha256')
    return f'SCRAM-SHA-256${ITERATIONS}:{b64(salt)}${b64(stored)}:{b64(server)}'


def verify(password, value):
    scheme, params, keys = value.split('$')
    iterations, salt = params.split(':')
    stored, server = keys.split(':')
    salted = hashlib.pbkdf2_hmac('sha256', password.encode('ascii'), base64.b64decode(salt), int(iterations))
    return (scheme == 'SCRAM-SHA-256'
            and hmac.compare_digest(hashlib.sha256(hmac.digest(salted, b'Client Key', 'sha256')).digest(), base64.b64decode(stored))
            and hmac.compare_digest(hmac.digest(salted, b'Server Key', 'sha256'), base64.b64decode(server)))


def main():
    target = Path.home() / '.codex' / 'private' / f'garuda-db-bootstrap-{PROJECT}'
    if target.exists():
        raise SystemExit('Private output directory already exists; refusing to overwrite credentials.')
    target.mkdir(parents=True, mode=0o700)
    if os.name == 'nt':
        identity = subprocess.check_output(['whoami', '/user', '/fo', 'csv', '/nh'], text=True)
        sid = next(csv.reader(io.StringIO(identity.strip())))[1]
        subprocess.run(['icacls', str(target), '/inheritance:r', '/grant:r', f'*{sid}:(OI)(CI)F'],
                       check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    else:
        target.chmod(0o700)
    passwords = {name: secrets.token_urlsafe(32) for name in ('apaar_app', 'apaar_migrator')}
    values = {name: verifier(password) for name, password in passwords.items()}
    assert passwords['apaar_app'] != passwords['apaar_migrator']
    assert len({value.split('$')[1].split(':')[1] for value in values.values()}) == 2
    assert all(verify(passwords[name], value) for name, value in values.items())
    template = Path(__file__).with_name('supabase_demo_bootstrap.sql').read_text(encoding='utf-8')
    rendered = template.replace('__APP_SCRAM_VERIFIER__', values['apaar_app']).replace('__MIGRATOR_SCRAM_VERIFIER__', values['apaar_migrator'])
    for name, content in {
        'role-passwords.private.json': json.dumps({'project_ref': PROJECT, 'passwords': passwords}, indent=2),
        '01-bootstrap.private.sql': rendered,
    }.items():
        path = target / name
        with path.open('x', encoding='utf-8') as output:
            output.write(content)
        if os.name != 'nt':
            path.chmod(0o600)
    print('Prepared two independent 256-bit passwords and salted SCRAM verifiers offline.')
    print('Private files saved under: ' + str(target))
    print('Project confirmation remains unfilled. No SQL or database connection was executed.')


if __name__ == '__main__':
    main()
