package com.fullnestjob.modules.shared.dto;

import java.util.List;

public class PaginationDtos {
    public static class PaginationQueryDTO {
        private Integer current;
        private Integer pageSize;
        private String sort; // e.g. "-updatedAt" or "name"

        // optional filters commonly used across modules
        private String name;
        private String address;
        private String location;
        private String apiPath;
        private String method;
        private String module;
        private String status;
        private String email;
        private String scope; // "public" or "admin" (optional)
        private List<String> skills;
        private List<String> locations; // multiple locations for filtering
        private String companyId; // optional: filter jobs by company
        private String excludeId; // optional: exclude a specific id (e.g., current job)
        private String salary; // optional: backward-compat string; prefers minSalary/maxSalary
        private Double minSalary; // optional: VND
        private Double maxSalary; // optional: VND
        private String level; // optional: CSV levels filter, e.g. "INTERN,FRESHER"

        public Integer getCurrent() { return current; }
        public void setCurrent(Integer current) { this.current = current; }
        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
        public String getSort() { return sort; }
        public void setSort(String sort) { this.sort = sort; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getApiPath() { return apiPath; }
        public void setApiPath(String apiPath) { this.apiPath = apiPath; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
        public List<String> getLocations() { return locations; }
        public void setLocations(List<String> locations) { this.locations = locations; }
        public String getCompanyId() { return companyId; }
        public void setCompanyId(String companyId) { this.companyId = companyId; }
        public String getExcludeId() { return excludeId; }
        public void setExcludeId(String excludeId) { this.excludeId = excludeId; }
        public String getSalary() { return salary; }
        public void setSalary(String salary) { this.salary = salary; }
        public Double getMinSalary() { return minSalary; }
        public void setMinSalary(Double minSalary) { this.minSalary = minSalary; }
        public Double getMaxSalary() { return maxSalary; }
        public void setMaxSalary(Double maxSalary) { this.maxSalary = maxSalary; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }

    public static class MetaDTO {
        public Integer current;
        public Integer pageSize;
        public Integer pages;
        public Integer total;
    }

    public static class PageResultDTO<T> {
        public MetaDTO meta;
        public List<T> result;
    }
}


