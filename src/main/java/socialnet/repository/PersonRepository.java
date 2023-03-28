package socialnet.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import socialnet.model.Person;

@RequiredArgsConstructor
@Repository
public class PersonRepository {

    private final JdbcTemplate jdbcTemplate;

    public int save(Person person) {
        return jdbcTemplate.update(
            "insert into persons (email, first_name, last_name, password, reg_date) values (?, ?, ?, ?, ?)",
            person.getEmail(),
            person.getFirstName(),
            person.getLastName(),
            person.getPassword(),
            person.getRegDate()
        );
    }

    public Person findByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(
                "select * from persons where email = ?",
                personRowMapper,
                email
            );
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    public Person findById(Integer authorId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT * FROM persons WHERE id = " + authorId,
                personRowMapper
            );
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private final RowMapper<Person> personRowMapper = (resultSet, rowNum) -> {
        Person person = new Person();
        person.setId(resultSet.getLong("id"));
        person.setAbout(resultSet.getString("about"));
        person.setBirthDate(resultSet.getTimestamp("birth_date"));
        person.setChangePasswordToken(resultSet.getString("change_password_token"));
        person.setConfigurationCode(resultSet.getInt("configuration_code"));
        person.setDeletedTime(resultSet.getTimestamp("deleted_time"));
        person.setEmail(resultSet.getString("email"));
        person.setFirstName(resultSet.getString("first_name"));
        person.setIsApproved(resultSet.getBoolean("is_approved"));
        person.setIsBlocked(resultSet.getBoolean("is_blocked"));
        person.setIsDeleted(resultSet.getBoolean("is_deleted"));
        person.setLastName(resultSet.getString("last_name"));
        person.setLastOnlineTime(resultSet.getTimestamp("last_online_time"));
        person.setMessagePermissions(resultSet.getString("message_permissions"));
        person.setNotificationsSessionId(resultSet.getString("notifications_session_id"));
        person.setOnlineStatus(resultSet.getString("online_status"));
        person.setPassword(resultSet.getString("password"));
        person.setPhone(resultSet.getString("phone"));
        person.setPhoto(resultSet.getString("photo"));
        person.setRegDate(resultSet.getTimestamp("reg_date"));
        person.setCity(resultSet.getString("city"));
        person.setCountry(resultSet.getString("country"));
        person.setTelegramId(resultSet.getLong("telegram_id"));
        person.setPersonSettingsId(resultSet.getLong("person_settings_id"));

        return person;
    };
}