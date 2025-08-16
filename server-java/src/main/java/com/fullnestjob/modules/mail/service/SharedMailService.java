package com.fullnestjob.modules.mail.service;

import com.fullnestjob.modules.jobs.entity.Job;
import com.fullnestjob.modules.jobs.repo.JobRepository;
import com.fullnestjob.modules.subscribers.dto.SubscribersDtos;
import com.fullnestjob.modules.users.entity.User;
import com.fullnestjob.modules.users.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SharedMailService {
    private static final Logger log = LoggerFactory.getLogger(SharedMailService.class);
    private final JavaMailSender mailSender;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.web.base-url:http://localhost:5173}")
    private String webBaseUrl;

    @Value("${app.web.job-detail-path:/job/{id}}")
    private String webJobDetailPath;

    @Value("${app.cron.mail.top-jobs-limit:5}")
    private int topJobsLimit;

    public SharedMailService(JavaMailSender mailSender, JobRepository jobRepository, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    private List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.fullnestjob.modules.subscribers.repo.SubscriberRepository subscriberRepository;

    @Value("${app.cron.mail.enabled:true}")
    private boolean mailCronEnabled;

    @Value("${app.cron.mail.expression:}")
    private String mailCronExpression;

    // Mặc định: chạy mỗi 30s nếu không set app.cron.mail.expression
    @Scheduled(cron = "${app.cron.mail.expression:0/30 * * * * *}")
    public void sendToAllSubscribers() {
        if (!mailCronEnabled) return;
        try {
            var subscribers = subscriberRepository.findAll();
            log.info("[CRON][mail] start, subscribers={} ", subscribers.size());
            if (subscribers.isEmpty()) {
                log.info("[CRON][mail] no subscribers, skip");
                return;
            }
            List<Job> jobs = getAllJobs(); // skills now eager
            log.info("[CRON][mail] jobs={} ", jobs.size());
            for (var s : subscribers) {
                if (s.getEmail() == null || s.getEmail().isBlank()) continue;
                try {
                    List<Job> filtered = filterJobsBySkills(jobs, s.getSkills());
                    sendSimpleJobsEmail(s.getEmail(), s.getName(), filtered);
                    log.info("[CRON][mail] sent to {}", s.getEmail());
                } catch (Exception e) {
                    log.error("[CRON][mail] failed to {}: {}", s.getEmail(), e.getMessage());
                }
            }
            log.info("[CRON][mail] done");
        } catch (Exception ignored) {}
    }

    private void sendSimpleJobsEmail(String to, String name, List<Job> jobs) {
        if (fromEmail == null || fromEmail.isBlank()) return;
        String subject = "Gợi ý việc làm phù hợp";
        // Chọn tối đa 10 job lương cao nhất
        List<Job> top = jobs.stream()
                .sorted((a,b) -> Double.compare(b.getSalary()!=null? b.getSalary():0.0, a.getSalary()!=null? a.getSalary():0.0))
                .limit(Math.max(1, topJobsLimit))
                .toList();

        String template = loadTemplate("templates/jobs.html");
        if (template == null) return;
        StringBuilder jobsHtml = new StringBuilder();
        for (Job j : top) {
            jobsHtml.append("<tr><td>");
            jobsHtml.append("<div style='font-size:16px;font-weight:600;margin-bottom:4px;'><a href='" + safe(urlForJob(j)) + "' target='_blank' style='text-decoration:none;color:#1677ff;'>" + safe(j.getName()) + "</a></div>");
            jobsHtml.append("<div style='font-size:14px;color:#595959;'>" + safe(j.getCompany()!=null? j.getCompany().getName(): "") + "</div>");
            jobsHtml.append("<div style='font-size:14px;color:#595959;margin:6px 0;'>" + (j.getSalary()!=null? String.format("%.0f đ", j.getSalary()) : "") + "</div>");
            if (j.getSkills()!=null) {
                jobsHtml.append("<div style='margin-top:6px;'>");
                for (String s : j.getSkills()) {
                    jobsHtml.append("<span style='display:inline-block;padding:2px 8px;margin:2px;border-radius:12px;background:#f0f5ff;color:#1d39c4;font-size:12px;'>"+ safe(s) +"</span>");
                }
                jobsHtml.append("</div>");
            }
            jobsHtml.append("<div style='margin:16px 0;border-top:1px dashed rgba(5,5,5,0.08);'></div>");
            jobsHtml.append("</td></tr>");
        }
        String html = template
                .replace("{{name}}", safe(name!=null? name : to))
                .replace("{{jobs_html}}", jobsHtml.toString())
                .replace("{{ctaUrl}}", safe(webBaseUrl + "/job"));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            // fallback plain text
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(html.replaceAll("<[^>]+>", ""));
            mailSender.send(msg);
        }
    }

    private String loadTemplate(String path) {
        try {
            java.io.InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
            if (is == null) return null;
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private String urlForJob(Job j) {
        String id = (j.get_id()!=null? j.get_id(): "");
        String path = webJobDetailPath.replace("{id}", id);
        if (!path.startsWith("/")) path = "/" + path;
        return webBaseUrl + path;
    }

    private String safe(String s) { return s == null ? "" : s; }

    private List<Job> filterJobsBySkills(List<Job> jobs, List<String> subsSkills) {
        if (subsSkills == null || subsSkills.isEmpty()) return jobs;
        java.util.Set<String> need = subsSkills.stream().map(x -> x.toLowerCase()).collect(java.util.stream.Collectors.toSet());
        return jobs.stream()
                .filter(j -> j.getSkills() != null && j.getSkills().stream().anyMatch(s -> need.contains(s.toLowerCase())))
                .toList();
    }

    public Map<String, Object> findAllJobsForEmail(String email) {
        List<Job> jobs = getAllJobs();
        return Map.of(
                "name", "",
                "jobs", jobs.stream().map(j -> Map.of(
                        "title", j.getName(),
                        "company", j.getCompany() != null ? j.getCompany().getName() : "",
                        "salary", j.getSalary() != null ? String.format("%.0f đ", j.getSalary()) : "",
                        "skills", j.getSkills()
                )).collect(Collectors.toList())
        );
    }

    public Object sendEmailTestForActiveUser() {
        // Placeholder: get active user from SecurityContext by id principal
        return Map.of("message", "Email sent");
    }
}


