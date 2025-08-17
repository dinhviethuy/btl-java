package com.fullnestjob.modules.ai.service;

import com.fullnestjob.modules.ai.dto.AiDtos.ChatRequestDTO;
import com.fullnestjob.modules.ai.dto.AiDtos.ChatResponseDTO;
import com.fullnestjob.modules.ai.dto.AiDtos.SuggestJobsRequestDTO;
import com.fullnestjob.modules.ai.dto.AiDtos.SuggestJobsResponseDTO;
import com.fullnestjob.modules.jobs.dto.JobDtos;
import com.fullnestjob.modules.jobs.service.JobsService;
import com.fullnestjob.modules.shared.dto.PaginationDtos;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiService {
    private final JobsService jobsService;

    public AiService(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    public ChatResponseDTO handleChat(ChatRequestDTO body) {
        ChatResponseDTO res = new ChatResponseDTO();
        String reply = "";
        String msg = Optional.ofNullable(body.message).orElse("").trim();
        if (msg.isBlank()) {
            reply = "Bạn có thể hỏi tôi về việc làm theo kỹ năng (React, Java...), địa điểm (Hà Nội, HCM...), mức lương, cấp bậc...";
            res.reply = reply;
            return res;
        }

        String normalized = nonAccentVietnamese(msg).toLowerCase();
        boolean isGreeting = containsAny(normalized, List.of("xin chao", "hello", "hi", "chao"));
        boolean askForSuggest = containsAny(normalized, List.of("goi y", "suggest", "recommend", "tim", "viec", "job"));
        boolean askForCount = containsAny(normalized, List.of("bao nhieu", "bao nhiêu", "may", "mấy"));

        if (isGreeting && !askForSuggest) {
            reply = "Xin chào! Tôi có thể giúp bạn tìm việc theo kỹ năng, địa điểm, hoặc gợi ý phù hợp. Ví dụ: 'Tìm React ở Hà Nội'.";
            res.reply = reply;
            return res;
        }

        // Thử trích xuất filters và trả lời dựa trên dữ liệu thật
        ExtractResult er = extractFilters(msg, null, null);
        boolean hasFilter = (er.skills != null && !er.skills.isEmpty()) || (er.locations != null && !er.locations.isEmpty());
        if (askForSuggest || hasFilter) {
            PaginationDtos.PaginationQueryDTO q = new PaginationDtos.PaginationQueryDTO();
            q.setCurrent(1);
            q.setPageSize(5);
            q.setScope("public");
            if (er.skills != null && !er.skills.isEmpty()) q.setSkills(er.skills);
            if (er.locations != null && !er.locations.isEmpty()) q.setLocations(er.locations);

            PaginationDtos.PageResultDTO<JobDtos.JobDetailDTO> page = jobsService.find(q);
            int total = page != null && page.meta != null && page.meta.total != null ? page.meta.total : (page != null && page.result != null ? page.result.size() : 0);

            StringBuilder sb = new StringBuilder();
            if (askForCount) {
                sb.append("Có ").append(total).append(" việc làm phù hợp");
            } else if (total > 0) {
                sb.append("Tôi tìm thấy ").append(total).append(" việc làm phù hợp");
            } else {
                sb.append("Hiện chưa tìm thấy việc làm phù hợp");
            }
            if (er.skills != null && !er.skills.isEmpty()) sb.append(" theo kỹ năng ").append(er.skills);
            if (er.locations != null && !er.locations.isEmpty()) sb.append(" tại ").append(er.locations);
            if (total > 0) sb.append(". Tôi đã hiển thị một số gợi ý bên dưới.");
            reply = sb.toString();
        } else {
            // fallback
            reply = "Tôi đã ghi nhận: '" + msg + "'. Bạn có thể nói rõ kỹ năng/địa điểm (VD: React Hà Nội) hoặc nhấn 'Gợi ý jobs'.";
        }
        res.reply = reply;
        return res;
    }

    public SuggestJobsResponseDTO suggestJobs(SuggestJobsRequestDTO body) {
        String query = Optional.ofNullable(body.query).orElse("");
        ExtractResult er = extractFilters(query, body.skills, body.locations);
        PaginationDtos.PaginationQueryDTO q = new PaginationDtos.PaginationQueryDTO();
        q.setCurrent(body.current != null ? body.current : 1);
        q.setPageSize(body.pageSize != null ? body.pageSize : 5);
        q.setScope("public");
        if (!er.skills.isEmpty()) q.setSkills(er.skills);
        if (!er.locations.isEmpty()) q.setLocations(er.locations);
        // Không parse salary/level ở backend hiện tại, có thể mở rộng sau

        PaginationDtos.PageResultDTO<JobDtos.JobDetailDTO> page = jobsService.find(q);

        SuggestJobsResponseDTO res = new SuggestJobsResponseDTO();
        res.jobs = page;
        res.normalizedQuery = toQueryString(er.skills, er.locations);
        res.reasoning = er.reasoning;
        return res;
    }

    private static class ExtractResult {
        List<String> skills = new ArrayList<>();
        List<String> locations = new ArrayList<>();
        String reasoning;
    }

    private ExtractResult extractFilters(String input, List<String> explicitSkills, List<String> explicitLocations) {
        ExtractResult r = new ExtractResult();
        String text = Optional.ofNullable(input).orElse("");
        // Danh sách kỹ năng và địa điểm đồng bộ với FE
        Map<String, String> skillMap = new LinkedHashMap<>();
        skillMap.put("react", "react.js");
        skillMap.put("reactjs", "react.js");
        skillMap.put("react native", "react native");
        skillMap.put("vue", "vue.js");
        skillMap.put("vuejs", "vue.js");
        skillMap.put("angular", "angular");
        skillMap.put("nest", "nest.js");
        skillMap.put("nestjs", "nest.js");
        skillMap.put("typescript", "typescript");
        skillMap.put("java", "java");
        skillMap.put("frontend", "frontend");
        skillMap.put("backend", "backend");
        skillMap.put("fullstack", "fullstack");

        Map<String, String> locationMap = new LinkedHashMap<>();
        locationMap.put("hà nội", "hanoi");
        locationMap.put("ha noi", "hanoi");
        locationMap.put("hanoi", "hanoi");
        locationMap.put("hồ chí minh", "hochiminh");
        locationMap.put("ho chi minh", "hochiminh");
        locationMap.put("hcm", "hochiminh");
        locationMap.put("hochiminh", "hochiminh");
        locationMap.put("đà nẵng", "danang");
        locationMap.put("da nang", "danang");
        locationMap.put("danang", "danang");

        String normalized = nonAccentVietnamese(text).toLowerCase();

        for (Map.Entry<String, String> e : skillMap.entrySet()) {
            if (normalized.contains(e.getKey())) r.skills.add(e.getValue().toUpperCase());
        }
        for (Map.Entry<String, String> e : locationMap.entrySet()) {
            if (normalized.contains(e.getKey())) r.locations.add(e.getValue().toUpperCase());
        }

        if (explicitSkills != null && !explicitSkills.isEmpty()) {
            r.skills.addAll(explicitSkills.stream().map(s -> s.toUpperCase()).collect(Collectors.toList()));
        }
        if (explicitLocations != null && !explicitLocations.isEmpty()) {
            r.locations.addAll(explicitLocations.stream().map(s -> s.toUpperCase()).collect(Collectors.toList()));
        }

        // unique
        r.skills = r.skills.stream().distinct().collect(Collectors.toList());
        r.locations = r.locations.stream().distinct().collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("Nhận diện: ");
        if (!r.skills.isEmpty()) sb.append("kỹ năng=").append(r.skills).append(" ");
        if (!r.locations.isEmpty()) sb.append("địa điểm=").append(r.locations).append(" ");
        r.reasoning = sb.toString().trim();
        return r;
    }

    private String toQueryString(List<String> skills, List<String> locations) {
        List<String> pairs = new ArrayList<>();
        if (skills != null) {
            for (String s : skills) pairs.add("skills=" + encode(s.toLowerCase()));
        }
        if (locations != null) {
            for (String l : locations) pairs.add("locations=" + encode(l.toLowerCase()));
        }
        return String.join("&", pairs);
    }

    private static boolean containsAny(String input, List<String> tokens) {
        String s = Optional.ofNullable(input).orElse("").toLowerCase();
        for (String t : tokens) if (s.contains(t)) return true;
        return false;
    }

    private static String nonAccentVietnamese(String str) {
        if (str == null) return null;
        String s = str;
        s = s.replaceAll("A|Á|À|Ã|Ạ|Â|Ấ|Ầ|Ẫ|Ậ|Ă|Ắ|Ằ|Ẵ|Ặ", "A");
        s = s.replaceAll("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a");
        s = s.replaceAll("E|É|È|Ẽ|Ẹ|Ê|Ế|Ề|Ễ|Ệ", "E");
        s = s.replaceAll("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e");
        s = s.replaceAll("I|Í|Ì|Ĩ|Ị", "I");
        s = s.replaceAll("ì|í|ị|ỉ|ĩ", "i");
        s = s.replaceAll("O|Ó|Ò|Õ|Ọ|Ô|Ố|Ồ|Ỗ|Ộ|Ơ|Ớ|Ờ|Ỡ|Ợ", "O");
        s = s.replaceAll("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o");
        s = s.replaceAll("U|Ú|Ù|Ũ|Ụ|Ư|Ứ|Ừ|Ữ|Ự", "U");
        s = s.replaceAll("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u");
        s = s.replaceAll("Y|Ý|Ỳ|Ỹ|Ỵ", "Y");
        s = s.replaceAll("ỳ|ý|ỵ|ỷ|ỹ", "y");
        s = s.replaceAll("Đ", "D");
        s = s.replaceAll("đ", "d");
        s = s.replaceAll("\\u0300|\\u0301|\\u0303|\\u0309|\\u0323", "");
        s = s.replaceAll("\\u02C6|\\u0306|\\u031B", "");
        return s;
    }

    private static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}


