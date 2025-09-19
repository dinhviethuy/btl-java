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
	private final com.fullnestjob.modules.companies.service.CompaniesService companiesService;
	private final com.fullnestjob.modules.resumes.repo.ResumeRepository resumeRepository;

	public UsersService(UserRepository userRepository, CompanyRepository companyRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, com.fullnestjob.modules.companies.service.CompaniesService companiesService, com.fullnestjob.modules.resumes.repo.ResumeRepository resumeRepository) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.companiesService = companiesService;
		this.resumeRepository = resumeRepository;
	}

	public PageResultDTO<UserDetailDTO> find(PaginationQueryDTO query) {
		int current = query.getCurrent() != null ? query.getCurrent() : 1;
		int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
		Sort sort = parseSort(query);
		var pageable = PageRequest.of(current - 1, pageSize, sort);

        String name = query.getName();
        String email = query.getEmail();
        String companyIdFilter = query.getCompanyId();
		if (name != null) name = name.replaceAll("^/+|/i$", "");
		if (email != null) email = email.replaceAll("^/+|/i$", "");

		Page<User> page;
		// Scope by company if current user is not SUPER_ADMIN (ADMIN cũng bị giới hạn)
		String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
		boolean isSuperAdmin = currentRole != null && currentRole.equalsIgnoreCase("SUPER_ADMIN");
		if (!isSuperAdmin) {
			String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
			User me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
			java.util.Set<String> companyIds = new java.util.LinkedHashSet<>();
			if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) companyIds.add(me.getCompany().get_id());
			for (Company c : companyRepository.findAllByCreatorId(currentUserId)) {
				if (c != null && c.get_id() != null) companyIds.add(c.get_id());
			}
			if (!companyIds.isEmpty()) {
				java.util.List<String> ids = new java.util.ArrayList<>(companyIds);
				if (name != null && email != null) {
					page = userRepository.findByCompanyIdsAndNameLikeAndEmailLike(ids, name, email, pageable);
				} else if (name != null) {
					page = userRepository.findByCompanyIdsAndNameLike(ids, name, pageable);
				} else if (email != null) {
					page = userRepository.findByCompanyIdsAndEmailLike(ids, email, pageable);
				} else {
					page = userRepository.findByCompanyIds(ids, pageable);
				}
			} else {
				page = Page.empty(pageable);
			}
        } else {
			if (companyIdFilter != null && !companyIdFilter.isBlank()) {
                if (name != null && email != null) {
                    page = userRepository.findByCompanyIdAndNameLikeAndEmailLike(companyIdFilter, name, email, pageable);
                } else if (name != null) {
                    page = userRepository.findByCompanyIdAndNameLike(companyIdFilter, name, pageable);
                } else if (email != null) {
                    page = userRepository.findByCompanyIdAndEmailLike(companyIdFilter, email, pageable);
                } else {
                    page = userRepository.findByCompanyId(companyIdFilter, pageable);
                }
            } else {
                if (name != null && email != null) {
                    page = userRepository.findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(name, email, pageable);
                } else if (name != null) {
                    page = userRepository.findByNameContainingIgnoreCase(name, pageable);
                } else if (email != null) {
                    page = userRepository.findByEmailContainingIgnoreCase(email, pageable);
                } else {
                    page = userRepository.findAll(pageable);
                }
            }
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
		// Validate duplicate email
		if (body.email != null && userRepository.findByEmail(body.email).orElse(null) != null) {
			throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Email already exists");
		}
		u.setPassword(passwordEncoder.encode(body.password));
		u.setName(body.name);
		u.setAge(body.age);
		u.setGender(body.gender);
		u.setAddress(body.address);
		// SUPER_ADMIN: có thể chọn bất kỳ company; Non-super-admin: chỉ chọn trong allowed companies (công ty mình đang ở hoặc mình tạo)
		String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
		boolean isSuperAdmin = currentRole != null && currentRole.equalsIgnoreCase("SUPER_ADMIN");
		if (isSuperAdmin) {
			if (body.company != null && body.company._id != null) {
				Company c = companyRepository.findById(body.company._id).orElse(null);
				u.setCompany(c);
			}
		} else {
			String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
			User me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
			java.util.Set<String> allowed = new java.util.LinkedHashSet<>();
			if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) allowed.add(me.getCompany().get_id());
			for (Company c : companyRepository.findAllByCreatorId(currentUserId)) {
				if (c != null && c.get_id() != null) allowed.add(c.get_id());
			}
			if (body.company != null && body.company._id != null && !body.company._id.isBlank()) {
				if (!allowed.contains(body.company._id)) {
					throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Cannot assign user to a company outside your allowed scope");
				}
				Company c = companyRepository.findById(body.company._id).orElse(null);
				u.setCompany(c);
			} else {
				if (me == null || me.getCompany() == null) {
					throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Insufficient permission to create user without company context");
				}
				u.setCompany(me.getCompany());
			}
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
		String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
		if (currentUserId != null && currentUserId.equals(id)) {
			throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Cannot update yourself");
		}
		User u = userRepository.findById(id).orElseThrow();
		String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
		boolean isSuperAdmin = currentRole != null && currentRole.equalsIgnoreCase("SUPER_ADMIN");
		if (!isSuperAdmin) {
			// Non-super-admin: chỉ quản lý trong phạm vi allowed companies (công ty mình đang ở hoặc công ty mình tạo)
			User me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
			java.util.Set<String> allowed = new java.util.LinkedHashSet<>();
			if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) allowed.add(me.getCompany().get_id());
			for (Company c : companyRepository.findAllByCreatorId(currentUserId)) {
				if (c != null && c.get_id() != null) allowed.add(c.get_id());
			}
			String targetCurrentCompanyId = u.getCompany() != null ? u.getCompany().get_id() : null;
			if (targetCurrentCompanyId != null && !allowed.contains(targetCurrentCompanyId)) {
				throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Cannot manage user outside your allowed companies");
			}
		}
		if (body.email != null) u.setEmail(body.email);
		if (body.name != null) u.setName(body.name);
		if (body.age != null) u.setAge(body.age);
		if (body.gender != null) u.setGender(body.gender);
		if (body.address != null) u.setAddress(body.address);
		if (body.company != null && body.company._id != null) {
			if (!isSuperAdmin) {
				java.util.Set<String> allowed = new java.util.LinkedHashSet<>();
				User me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
				if (me != null && me.getCompany() != null && me.getCompany().get_id() != null) allowed.add(me.getCompany().get_id());
				for (Company c0 : companyRepository.findAllByCreatorId(currentUserId)) {
					if (c0 != null && c0.get_id() != null) allowed.add(c0.get_id());
				}
				if (!allowed.contains(body.company._id)) {
					throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Cannot change user's company outside your allowed companies");
				}
			}
			Company c = companyRepository.findById(body.company._id).orElse(null);
			u.setCompany(c);
		} else if (body.company != null && (body.company._id == null || body.company._id.isBlank())) {
			// Explicit request to detach user from company (companyId = null)
			u.setCompany(null);
		} else if (body.company == null) {
			// Explicit null means detach
			u.setCompany(null);
		}
		// Handle role assignment/detach. Accept null or blank to clear role.
		if (body.role != null && !body.role.isBlank()) {
			Role role = roleRepository.findById(body.role).orElse(null);
			if (!isSuperAdmin) {
				String targetCurrentRoleName = u.getRole() != null ? u.getRole().getName() : null;
				if (targetCurrentRoleName != null && (targetCurrentRoleName.equalsIgnoreCase("ADMIN") || targetCurrentRoleName.equalsIgnoreCase("SUPER_ADMIN"))) {
					throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Insufficient permission to change admin role");
				}
				if (role != null && (role.getName().equalsIgnoreCase("ADMIN") || role.getName().equalsIgnoreCase("SUPER_ADMIN"))) {
					throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Insufficient permission to assign admin role");
				}
			}
			u.setRole(role);
		} else {
			// Explicit null or blank => detach current role
			if (!isSuperAdmin) {
				String targetCurrentRoleName = u.getRole() != null ? u.getRole().getName() : null;
				if (targetCurrentRoleName != null && (targetCurrentRoleName.equalsIgnoreCase("ADMIN") || targetCurrentRoleName.equalsIgnoreCase("SUPER_ADMIN"))) {
					throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Insufficient permission to remove admin role");
				}
			}
			u.setRole(null);
		}
		return toDetail(userRepository.save(u));
	}

	@Transactional
	public void delete(String id) {
		String currentUserId = com.fullnestjob.security.SecurityUtils.getCurrentUserId();
		if (currentUserId != null && currentUserId.equals(id)) {
			throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Cannot delete yourself");
		}
		String currentRole = com.fullnestjob.security.SecurityUtils.getCurrentRole();
		boolean isSuperAdmin = currentRole != null && currentRole.equalsIgnoreCase("SUPER_ADMIN");
		if (!isSuperAdmin) {
			User target = userRepository.findById(id).orElseThrow();
			User me = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
			String myCompanyId = me != null && me.getCompany() != null ? me.getCompany().get_id() : null;
			String targetCompanyId = target.getCompany() != null ? target.getCompany().get_id() : null;
			if (myCompanyId == null || targetCompanyId == null || !myCompanyId.equals(targetCompanyId)) {
				throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Cannot delete user outside your company");
			}
		}

		// Cascade: delete all companies created by this user
		java.util.List<Company> createdCompanies = companyRepository.findAllByCreatorId(id);
		if (createdCompanies != null && !createdCompanies.isEmpty()) {
			for (Company c : createdCompanies) {
				if (c != null && c.get_id() != null) {
					// CompaniesService.delete will: set users' company to null; delete jobs; delete resumes; then delete company
					companiesService.delete(c.get_id());
				}
			}
		}
		// Delete all resumes submitted by this user
		java.util.List<com.fullnestjob.modules.resumes.entity.Resume> myResumes = resumeRepository.findByUserId(id);
		if (myResumes != null && !myResumes.isEmpty()) {
			resumeRepository.deleteAll(myResumes);
		}
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


