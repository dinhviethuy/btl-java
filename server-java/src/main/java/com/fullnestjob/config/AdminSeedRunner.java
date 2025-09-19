package com.fullnestjob.config;

import com.fullnestjob.modules.roles.entity.Role;
import com.fullnestjob.modules.roles.repo.RoleRepository;
import com.fullnestjob.modules.users.entity.User;
import com.fullnestjob.modules.users.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Order(10)
public class AdminSeedRunner implements CommandLineRunner {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.email:}")
	private String adminEmail;
	@Value("${app.admin.password:}")
	private String adminPassword;
	@Value("${app.admin.name:Admin}")
	private String adminName;
	@Value("${app.admin.address:}")
	private String adminAddress;
	@Value("${app.admin.age:0}")
	private Integer adminAge;
	@Value("${app.admin.gender:}")
	private String adminGender;

	public AdminSeedRunner(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
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
        // Chỉ chạy khi có truyền cờ cụ thể, tránh auto-run khi bootRun không có args
        boolean shouldRun = hasAny(args, "--seed-admin", "--seed.admin", "--seed-all", "--seed.all");
		if (!shouldRun) return;

		if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
			System.out.println("AdminSeedRunner: skipped (missing app.admin.email or app.admin.password)");
			return;
		}

		// Ensure SUPER_ADMIN role
		Role superAdmin = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
			Role r = new Role();
			r.setName("SUPER_ADMIN");
			r.setDescription("Super administrator");
			r.setIsActive(true);
			return roleRepository.save(r);
		});

		// Ensure admin user
		User exist = userRepository.findByEmail(adminEmail).orElse(null);
		if (exist == null) {
			User u = new User();
			u.setEmail(adminEmail);
			u.setPassword(passwordEncoder.encode(adminPassword));
			u.setName(adminName);
			u.setAddress(adminAddress);
			u.setAge(adminAge != null ? adminAge : 0);
			u.setGender(adminGender);
			u.setRole(superAdmin);
			userRepository.save(u);
			System.out.println("AdminSeedRunner: admin created " + adminEmail);
		} else {
			if (exist.getRole() == null || !"SUPER_ADMIN".equals(exist.getRole().getName())) {
				exist.setRole(superAdmin);
				userRepository.save(exist);
			}
			System.out.println("AdminSeedRunner: admin ensured " + adminEmail);
		}
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


