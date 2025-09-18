package com.fullnestjob.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.companies.repo.CompanyRepository;
import com.fullnestjob.modules.jobs.entity.Job;
import com.fullnestjob.modules.jobs.repo.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Order(30)
public class SeedRunner implements CommandLineRunner {
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    public SeedRunner(CompanyRepository companyRepository, JobRepository jobRepository) {
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean hasSpecificFlag = hasAny(args,
                "--seed-admin", "--seed.admin",
                "--seed-permissions", "--seed.permissions",
                "--seed-data", "--seed.data",
                "--seed-logos", "--seed.logos",
                "--seed-logos-direct", "--seed.logos.direct",
                "--seed-all", "--seed.all");
        boolean logosOnly = hasAny(args, "--seed-logos", "--seed.logos", "--seed-logos-direct", "--seed.logos.direct");
        boolean shouldRun = (!hasSpecificFlag && !logosOnly)
                || hasAny(args, "--seed-data", "--seed.data", "--seed-all", "--seed.all");
        if (!shouldRun) {
            System.out.println("SeedRunner: skipped (flags)");
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        Path appDir = Paths.get(System.getProperty("user.dir"));
        // Try candidates: current dir, parent dir
        List<Path> candidates = List.of(
                appDir.resolve("data/companies.json"),
                appDir.getParent() != null ? appDir.getParent().resolve("data/companies.json") : appDir.resolve("data/companies.json")
        );
        Optional<Path> companiesPath = candidates.stream().filter(Files::exists).findFirst();

        candidates = List.of(
                appDir.resolve("data/jobs.json"),
                appDir.getParent() != null ? appDir.getParent().resolve("data/jobs.json") : appDir.resolve("data/jobs.json")
        );
        Optional<Path> jobsPath = candidates.stream().filter(Files::exists).findFirst();

        int companiesInserted = 0;
        if (companiesPath.isPresent()) {
            try {
                JsonNode arr = mapper.readTree(Files.newInputStream(companiesPath.get()));
                if (arr.isArray()) {
                    for (JsonNode node : arr) {
                        String name = optText(node, "name");
                        if (name == null || name.isBlank()) continue;
                        Company c = companyRepository.findByName(name).orElseGet(Company::new);
                        if (c.get_id() == null) {
                            c.set_id(UUID.randomUUID().toString());
                        }
                        c.setName(name);
                        c.setAddress(optText(node, "address"));
                        c.setDescription(optText(node, "description"));
                        companyRepository.save(c);
                        companiesInserted++;
                    }
                }
                System.out.println("SeedRunner: companies processed=" + companiesInserted + " from " + companiesPath.get());
            } catch (IOException e) {
                System.out.println("SeedRunner: failed to parse data/companies.json: " + e.getMessage());
            }
        } else {
            System.out.println("SeedRunner: data/companies.json not found in " + appDir + " or parent");
        }

        int jobsInserted = 0;
        if (jobsPath.isPresent()) {
            try {
                JsonNode arr = mapper.readTree(Files.newInputStream(jobsPath.get()));
                if (arr.isArray()) {
                    for (JsonNode node : arr) {
                        String name = optText(node, "name");
                        if (name == null || name.isBlank()) continue;
                        Job j = new Job();
                        j.set_id(UUID.randomUUID().toString());
                        j.setName(name);
                        j.setLocation(optText(node, "location"));
                        if (node.has("salary") && node.get("salary").isNumber()) {
                            j.setSalary(node.get("salary").asDouble());
                        }
                        if (node.has("quantity") && node.get("quantity").isInt()) {
                            j.setQuantity(node.get("quantity").asInt());
                        }
                        j.setLevel(optText(node, "level"));
                        j.setDescription(optText(node, "description"));
                        if (node.has("skills") && node.get("skills").isArray()) {
                            List<String> skills = new ArrayList<>();
                            node.get("skills").forEach(s -> { if (s.isTextual()) skills.add(s.asText()); });
                            j.setSkills(skills);
                        }
                        if (node.has("company")) {
                            JsonNode comp = node.get("company");
                            String companyName = optText(comp, "name");
                            if (companyName != null) {
                                companyRepository.findByName(companyName).ifPresent(j::setCompany);
                            }
                        }
                        jobRepository.save(j);
                        jobsInserted++;
                    }
                }
                System.out.println("SeedRunner: jobs processed=" + jobsInserted + " from " + jobsPath.get());
            } catch (IOException e) {
                System.out.println("SeedRunner: failed to parse data/jobs.json: " + e.getMessage());
            }
        } else {
            System.out.println("SeedRunner: data/jobs.json not found in " + appDir + " or parent");
        }
    }

    private static String optText(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return null;
        JsonNode v = node.get(field);
        return v.isTextual() ? v.asText() : v.toString();
    }

    private static boolean hasAny(String[] args, String... keys) {
        if (args == null || args.length == 0) return false;
        for (String a : args) {
            for (String k : keys) {
                if (k.equals(a)) return true;
            }
        }
        return false;
    }
}


