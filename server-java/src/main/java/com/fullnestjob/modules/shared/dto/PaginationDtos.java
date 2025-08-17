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


