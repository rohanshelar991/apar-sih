package com.infobro.apaar.controller;
import com.infobro.apaar.service.VerificationWorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import jakarta.validation.Valid;import jakarta.validation.constraints.NotBlank;import jakarta.validation.constraints.Size;import java.util.*;
@RestController @RequestMapping("/api/verification")
public class VerificationWorkflowController {
 private final VerificationWorkflowService service; public VerificationWorkflowController(VerificationWorkflowService service){this.service=service;}
 @PostMapping("/students/{studentId}/requests") public ResponseEntity<Map<String,Object>> submit(@PathVariable Long studentId,Authentication auth,HttpServletRequest req){return ResponseEntity.status(201).body(service.submit(studentId,auth.getName(),req.getRemoteAddr()));}
 @GetMapping("/requests") public List<Map<String,Object>> queue(Authentication auth){return service.queue(auth.getName());}
 @GetMapping("/requests/{id}/history") public List<Map<String,Object>> history(@PathVariable Long id,Authentication auth){return service.history(id,auth.getName());}
 @PostMapping("/requests/{id}/decision") public Map<String,Object> decide(@PathVariable Long id,@Valid @RequestBody Decision body,Authentication auth,HttpServletRequest req){return service.decide(id,body.status(),body.remarks(),auth.getName(),req.getRemoteAddr());}
 @PostMapping("/requests/{id}/assignment") public Map<String,Object> assign(@PathVariable Long id,@Valid @RequestBody Assignment body,Authentication auth,HttpServletRequest req){return service.assign(id,body.assignee(),body.priority(),auth.getName(),req.getRemoteAddr());}
 @PostMapping("/requests/assign") public List<Map<String,Object>> assignBatch(@Valid @RequestBody BatchAssignment body,Authentication auth,HttpServletRequest req){return service.assignBatch(body.requestIds(),body.assignee(),body.priority(),auth.getName(),req.getRemoteAddr());}
 @GetMapping("/notifications") public List<Map<String,Object>> notifications(Authentication auth){return service.myNotifications(auth.getName());}
 @PostMapping("/notifications/{id}/read") public ResponseEntity<Void> markRead(@PathVariable Long id,Authentication auth){service.markRead(id,auth.getName());return ResponseEntity.noContent().build();}
 public record Decision(@NotBlank String status,@Size(max=1000) String remarks){}
 public record Assignment(@NotBlank @Size(max=120) String assignee,@Size(max=12) String priority){}
 public record BatchAssignment(@jakarta.validation.constraints.NotEmpty @jakarta.validation.constraints.Size(max=100) List<Long> requestIds,@NotBlank @Size(max=120) String assignee,@Size(max=12) String priority){}
}
