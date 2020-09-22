package at.jku.dke.etutor.service;

import at.jku.dke.etutor.config.Constants;
import at.jku.dke.etutor.domain.*;
import at.jku.dke.etutor.repository.*;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.dto.UserDTO;
import io.github.jhipster.security.RandomUtil;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;

    //region Repositories
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final AdministratorRepository administratorRepository;
    private final InstructorRepository instructorRepository;
    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;
    //endregion


    /**
     * Constructor.
     *
     * @param userRepository          the injected user repository
     * @param passwordEncoder         the injected password encoder
     * @param authorityRepository     the injected authority repository
     * @param administratorRepository the injected administrator repository
     * @param instructorRepository    the injected instructor repository
     * @param studentRepository       the injected student repository
     * @param tutorRepository         the injected tutor repository
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository,
                       AdministratorRepository administratorRepository, InstructorRepository instructorRepository,
                       StudentRepository studentRepository, TutorRepository tutorRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.administratorRepository = administratorRepository;
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                return user;
            });
    }

    /**
     * Creates a new user entity based on the given user dto.
     *
     * @param userDTO the base dto for the new user
     * @return the new user entity
     */
    @Transactional
    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(user.getLogin());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities().stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        user = userRepository.save(user);
        saveUserRoles(user, userDTO);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Saves the new user roles based on the user's authorities.
     *
     * @param user    the user entity
     * @param userDTO the corresponding user dto
     */
    private void saveUserRoles(User user, UserDTO userDTO) {
        if (user == null) {
            throw new IllegalArgumentException("The parameter 'user' must not be null!");
        }
        if (userDTO == null) {
            throw new IllegalArgumentException("The parameter 'userDTO' must not be null!");
        }

        //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        if (userDTO.getAuthorities() != null) {
            if (userDTO.getAuthorities().contains(AuthoritiesConstants.ADMIN)) {
                Administrator admin = new Administrator();
                admin.setUser(user);
                admin = administratorRepository.save(admin);
                user.addPerson(admin);
            }
            if (userDTO.getAuthorities().contains(AuthoritiesConstants.INSTRUCTOR)) {
                Instructor instructor = new Instructor();
                instructor.setUser(user);
                instructor = instructorRepository.save(instructor);
                user.addPerson(instructor);
            }
            if (userDTO.getAuthorities().contains(AuthoritiesConstants.STUDENT)) {
                Student student = new Student();
                student.setUser(user);
                student = studentRepository.save(student);
                user.addPerson(student);
            }
            if (userDTO.getAuthorities().contains(AuthoritiesConstants.TUTOR)) {
                Tutor tutor = new Tutor();
                tutor.setUser(user);
                tutor = tutorRepository.save(tutor);
                user.addPerson(tutor);
            }
        }
    }

    /**
     * Manages the user associated roles.
     *
     * @param user the user to update
     */
    private void updateUserRoles(User user) {
        var associatedAuthorities = StreamEx.of(user.getAuthorities()).map(x -> AuthoritiesConstants.getClassNameByRoleConstant(x.getName())).nonNull().toList();
        Iterator<Person> personIt = user.getAssociatedPersons().iterator();

        //Remove non existing roles.
        while (personIt.hasNext()) {
            Person p = personIt.next();
            if (!associatedAuthorities.contains(p.getClass().getName())) {
                personIt.remove();
            }
        }

        //Add new roles
        var roleList = StreamEx.of(user.getAssociatedPersons()).map(x -> x.getClass().getName()).toList();
        for (var authority : associatedAuthorities) {
            if (!roleList.contains(authority)) {
                try {
                    Person newPerson = (Person) Class.forName(authority).getDeclaredConstructor().newInstance();

                    user.addPerson(newPerson);
                } catch (Exception e) {
                    log.error("An exception occurred - should not happen!", e);
                }
            }
        }
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                updateUserRoles(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                log.debug("Changed Information for User: {}", user);
            });
    }


    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                log.debug("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                log.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.getClientAuthorities();
    }
}
