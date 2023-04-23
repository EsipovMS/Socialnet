package socialnet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import socialnet.api.request.LoginRq;
import socialnet.api.request.UserRq;
import socialnet.api.request.UserUpdateDto;
import socialnet.api.response.CommonRs;
import socialnet.api.response.ComplexRs;
import socialnet.api.response.ErrorRs;
import socialnet.api.response.PersonRs;
import socialnet.exception.EmptyEmailException;
import socialnet.mappers.PersonMapper;
import socialnet.mappers.UserDtoMapper;
import socialnet.model.Person;
import socialnet.repository.PersonRepository;
import socialnet.security.jwt.JwtUtils;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class PersonService {
    private final JwtUtils jwtUtils;
    private String jwt;
    private final AuthenticationManager authenticationManager;
    private final PersonRepository personRepository;
    private final WeatherService weatherService;
    private final CurrencyService currencyService;
    private final UserDtoMapper userDtoMapper;

    public Object getLogin(LoginRq loginRq) {

        Person person;
        if ((person = checkLoginAndPassword(loginRq.getEmail(), loginRq.getPassword())) != null) {
            jwt = jwtUtils.generateJwtToken(loginRq.getEmail());
            authenticated(loginRq);
            PersonRs personRs = PersonMapper.INSTANCE.toDTO(person);
            personRs.setToken(jwt);
            personRs.setOnline(true);
            personRs.setIsBlockedByCurrentUser(false);
            personRs.setWeather(weatherService.getWeatherByCity(person.getCity()));
            personRs.setCurrency(currencyService.getCurrency(LocalDate.now()));

            return new CommonRs<>(personRs);
        } else {
            throw new EmptyEmailException("invalid username or password");
        }
    }

    public CommonRs<PersonRs> getMyProfile(String authorization) {
        String email = jwtUtils.getUserEmail(authorization);
        Person person = personRepository.findByEmail(email);
        PersonRs personRs = PersonMapper.INSTANCE.toDTO(person);
        personRs.setToken(jwt);
        personRs.setOnline(true);
        personRs.setIsBlockedByCurrentUser(false);
        personRs.setWeather(weatherService.getWeatherByCity(person.getCity()));
        personRs.setCurrency(currencyService.getCurrency(LocalDate.now()));
        return new CommonRs<>(personRs);
    }

    public ResponseEntity<?> getUserInfo(String authorization) {

        if (!jwtUtils.validateJwtToken(authorization)) {//401
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String userName = jwtUtils.getUserEmail(authorization);
        if (userName.isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorRs("EmptyEmailException", "Field 'email' is empty"), HttpStatus.BAD_REQUEST);  //400
        }

        Person person = personRepository.findByEmail(userName);
        if (person.getIsDeleted()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);  //403
        }

        PersonRs personRs = PersonMapper.INSTANCE.toDTO(person);

        personRs.setWeather(weatherService.getWeatherByCity(person.getCity()));
        personRs.setCurrency(currencyService.getCurrency(LocalDate.now()));

        return ResponseEntity.ok(new CommonRs(personRs));
    }

    public Object getLogout(String authorization) {

        return setCommonRs(setComplexRs());
    }

    public Object getUserById(String authorization, Integer id) {
        Person person = findUser(id);
        PersonRs personRs = PersonMapper.INSTANCE.toDTO(person);
        personRs.setToken(jwt);
        personRs.setOnline(true);
        personRs.setIsBlockedByCurrentUser(false);
        personRs.setWeather(weatherService.getWeatherByCity("Sochi"));
        personRs.setCurrency(currencyService.getCurrency(LocalDate.now()));
        return new CommonRs<>(personRs);
    }

    private Person findUser(Integer id) {
        return personRepository.findById(Long.valueOf(id));
    }

    public CommonRs<ComplexRs> setCommonRs(ComplexRs complexRs) {
        CommonRs<ComplexRs> commonRs = new CommonRs<>();
        commonRs.setData(complexRs);
        commonRs.setOffset(0);
        commonRs.setTimestamp(0L);
        commonRs.setTotal(0L);
        commonRs.setItemPerPage(0);
        commonRs.setPerPage(0);
        return commonRs;
    }

    public ComplexRs setComplexRs() {

        return ComplexRs.builder()
                .id(0)
                .count(0L)
                .message("")
                .messageId(0L)
                .build();
    }

    public Person getAuthPerson() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return personRepository.getPersonByEmail(email);
    }

    public Person checkLoginAndPassword(String email, String password) {

        Person person = personRepository.findByEmail(email);

        if (person != null && new BCryptPasswordEncoder().matches(password, person.getPassword())) {
            log.info(person.getFirstName() + " авторизован");
            return person;
        }
        return null;
    }

    private void authenticated(LoginRq loginRq) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRq.getEmail(), loginRq.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public ResponseEntity<?> updateUserInfo(String authorization, UserRq userRq) {

        if (!jwtUtils.validateJwtToken(authorization)) {//401
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String userName = jwtUtils.getUserEmail(authorization);
        if (userName.isEmpty()) {
            return new ResponseEntity<>(
                    new ErrorRs("EmptyEmailException", "Field 'email' is empty"),
                    HttpStatus.BAD_REQUEST);  //400
        }

        Person person = personRepository.findByEmail(userName);
        if (person.getIsBlocked()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);  //403
        }

        PersonRs personRs = PersonMapper.INSTANCE.toDTO(person);

        UserUpdateDto userUpdateDto = userDtoMapper.toDto(userRq);

        personRepository.updatePersonInfo(userUpdateDto, person.getEmail());

        return ResponseEntity.ok(new CommonRs(personRs));
    }

}