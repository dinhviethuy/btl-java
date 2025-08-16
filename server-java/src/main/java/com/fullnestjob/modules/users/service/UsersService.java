package com.fullnestjob.modules.users.service;

import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.companies.repo.CompanyRepository;
import com.fullnestjob.modules.roles.entity.Role;
import com.fullnestjob.modules.roles.repo.RoleRepository;
import com.fullnestjob.modules.shared.dto.PaginationDtos.MetaDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import com.fullnestjob.modules.users.dto.UserDtos.CreateUserBodyDTO;
import com.fullnestjob.modules.users.dto.UserDtos.RoleRefDTO;
import com.fullnestjob.modules.users.dto.UserDtos.UpdateUserBodyDTO;
import com.fullnestjob.modules.users.dto.UserDtos.UserDetailDTO;
import com.fullnestjob.modules.users.entity.User;
import com.fullnestjob.modules.users.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UsersService {
	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UsersService(UserRepository userRepository, CompanyRepository companyRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public PageResultDTO<UserDetailDTO> find(PaginationQueryDTO query) {
		int current = query.getCurrent() != null ? query.getCurrent() : 1;
		int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
		Sort sort = parseSort(query);
		var pageable = PageRequest.of(current - 1, pageSize, sort);

		String name = query.getName();
		String email = query.getEmail();
		if (name != null) name = name.replaceAll("^/+|/i$", "");
		if (email != null) email = email.replaceAll("^/+|/i$", "");

		Page<User> page;
		if (name != null && email != null) {
			page = userRepository.findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(name, email, pageable);
		} else if (name != null) {
			page = userRepository.findByNameContainingIgnoreCase(name, pageable);
		} else if (email != null) {
			page = userRepository.findByEmailContainingIgnoreCase(email, pageable);
		} else {
			page = userRepository.findAll(pageable);
		}

		PageResultDTO<UserDetailDTO> res = new PageResultDTO<>();
		res.result = page.getContent().stream().map(this::toDetail).collect(Collectors.toList());
		MetaDTO meta = new MetaDTO();
		meta.current = current;
		meta.pageSize = pageSize;
		meta.total = (int) page.getTotalElements();
		meta.pages = page.getTotalPages();
		res.meta = meta;
		return res;
	}

	public UserDetailDTO findById(String id) {
		return userRepository.findById(id).map(this::toDetail).orElse(null);
	}

	@Transactional
	public UserDetailDTO create(CreateUserBodyDTO body) {
		User u = new User();
		u.setEmail(body.email);
		u.setPassword(passwordEncoder.encode(body.password));
		u.setName(body.name);
		u.setAge(body.age);
		u.setGender(body.gender);
		u.setAddress(body.address);
		if (body.company != null && body.company._id != null) {
			Company c = companyRepository.findById(body.company._id).orElse(null);
			u.setCompany(c);
		}
		if (body.role != null) {
			Role role = roleRepository.findById(body.role).orElse(null);
			u.setRole(role);
		}
		User saved = userRepository.save(u);
		return toDetail(saved);
	}

	@Transactional
	public UserDetailDTO update(String id, UpdateUserBodyDTO body) {
		User u = userRepository.findById(id).orElseThrow();
		if (body.email != null) u.setEmail(body.email);
		if (body.name != null) u.setName(body.name);
		if (body.age != null) u.setAge(body.age);
		if (body.gender != null) u.setGender(body.gender);
		if (body.address != null) u.setAddress(body.address);
		if (body.company != null && body.company._id != null) {
			Company c = companyRepository.findById(body.company._id).orElse(null);
			u.setCompany(c);
		}
		if (body.role != null) {
			Role role = roleRepository.findById(body.role).orElse(null);
			u.setRole(role);
		}
		return toDetail(userRepository.save(u));
	}

	@Transactional
	public void delete(String id) {
		userRepository.deleteById(id);
	}

	private UserDetailDTO toDetail(User u) {
		UserDetailDTO dto = new UserDetailDTO();
		dto._id = u.get_id();
		dto.email = u.getEmail();
		dto.name = u.getName();
		dto.age = u.getAge();
		dto.gender = u.getGender();
		dto.address = u.getAddress();
		if (u.getCompany() != null) {
			dto.company = new com.fullnestjob.modules.users.dto.UserDtos.CompanyRefDTO();
			dto.company._id = u.getCompany().get_id();
			dto.company.name = u.getCompany().getName();
		}
		if (u.getRole() != null) {
			RoleRefDTO r = new RoleRefDTO();
			r._id = u.getRole().get_id();
			r.name = u.getRole().getName();
			dto.role = r;
		}
		dto.createdAt = u.getCreatedAt();
		dto.updatedAt = u.getUpdatedAt();
		dto.deletedAt = u.getDeletedAt();
		return dto;
	}

	private Sort parseSort(PaginationQueryDTO query) {
		if (query == null) return Sort.by(Sort.Order.desc("updatedAt"));
		String s = query.getSort();
		if (s != null && !s.isBlank()) {
			String key = s.startsWith("sort=") ? s.substring(5) : s;
			if (key.startsWith("-")) return Sort.by(Sort.Order.desc(key.substring(1)));
			return Sort.by(Sort.Order.asc(key));
		}
		return Sort.by(Sort.Order.desc("updatedAt"));
	}
}


