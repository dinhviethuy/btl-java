package com.fullnestjob.config;

import com.fullnestjob.modules.permissions.entity.Permission;
import com.fullnestjob.modules.permissions.repo.PermissionRepository;
import com.fullnestjob.modules.roles.entity.Role;
import com.fullnestjob.modules.roles.repo.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Order(20)
public class PermissionsSeedRunner implements CommandLineRunner {
	private final RequestMappingHandlerMapping handlerMapping;
	private final PermissionRepository permissionRepository;
	private final RoleRepository roleRepository;

	public PermissionsSeedRunner(RequestMappingHandlerMapping handlerMapping, PermissionRepository permissionRepository, RoleRepository roleRepository) {
		this.handlerMapping = handlerMapping;
		this.permissionRepository = permissionRepository;
		this.roleRepository = roleRepository;
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
		boolean shouldRun = !hasSpecificFlag || hasAny(args, "--seed-permissions", "--seed.permissions", "--seed-all", "--seed.all");
		if (!shouldRun) return;

		Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
		List<Permission> created = new ArrayList<>();
		int scanned = 0;
		for (Map.Entry<RequestMappingInfo, HandlerMethod> e : map.entrySet()) {
			RequestMappingInfo info = e.getKey();
			Set<String> patterns = info.getPatternValues();
			Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
			if (patterns == null || patterns.isEmpty() || methods == null || methods.isEmpty()) continue;
			for (String p : patterns) {
				if (!p.startsWith("/v1")) continue; // Only API routes under /api/v1
				String apiPath = "/api" + p;
				for (RequestMethod m : methods) {
					if (m == RequestMethod.HEAD || m == RequestMethod.OPTIONS) continue;
					scanned++;
					String module = guessModuleFromPath(apiPath);
					String method = m.name();
					String permName = buildPermissionName(module, method, apiPath);
					Permission perm = ensurePermission(permName, apiPath, method, module);
					created.add(perm);
				}
			}
		}
		attachAllToSuperAdmin(created);
		System.out.println("PermissionsSeedRunner: scanned=" + scanned + ", ensured permissions=" + created.size());
	}

	private Permission ensurePermission(String name, String apiPath, String method, String module) {
		Permission p = permissionRepository.findByApiPathAndMethodAndModule(apiPath, method, module).orElse(null);
		if (p == null) {
			p = new Permission();
			p.setApiPath(apiPath);
			p.setMethod(method);
			p.setModule(module);
		}
		if (p.getName() == null || !p.getName().equals(name)) {
			p.setName(name);
		}
		return permissionRepository.save(p);
	}

	@Transactional
	private void attachAllToSuperAdmin(List<Permission> permissions) {
		Role role = roleRepository.findByNameFetchPermissions("SUPER_ADMIN").orElseGet(() -> {
			Role r = new Role();
			r.setName("SUPER_ADMIN");
			r.setDescription("Super administrator");
			r.setIsActive(true);
			return roleRepository.save(r);
		});
		java.util.Set<String> existing = new java.util.HashSet<>();
		if (role.getPermissions() != null) {
			for (Permission p : role.getPermissions()) {
				existing.add(p.get_id());
			}
		}
		for (Permission p : permissions) {
			if (p.get_id() != null && !existing.contains(p.get_id())) {
				role.getPermissions().add(p);
			}
		}
		roleRepository.save(role);
	}

	private static String guessModuleFromPath(String path) {
		if (path == null) return "API";
		String[] parts = path.split("/");
		// expect ["", "api", "v1", "{module}", ...]
		if (parts.length >= 4) {
			return parts[3].toUpperCase();
		}
		return "API";
	}

	private static String buildPermissionName(String module, String method, String path) {
		String cleaned = path.replace("/", "_").replace("{", "").replace("}", "");
		String name = module + "_" + method + "_" + cleaned;
		return name.length() > 100 ? name.substring(0, 100) : name;
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


