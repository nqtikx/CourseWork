package com.example;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс, представляющий студента с данными о его посещаемости.
 * Хранит информацию о фамилии, имени, отчестве, группе, посещениях и отсутствиях.
 *
 * @author Soldatov N. V.
 * @version 22.0.2
 */
public class Student {
    private String lastName;
    private String firstName;
    private String patronymic; // Отчество
    private String group;
    private String fullName;
    private final Set<String> attendanceDates = new HashSet<>(); // Даты посещений
    private final Set<String> absenceDates = new HashSet<>();    // Даты отсутствий

    /**
     * Конструктор для создания студента с указанием фамилии, имени, отчества и группы.
     * Полное имя студента будет сформировано автоматически.
     *
     * @param lastName    Фамилия студента.
     * @param firstName   Имя студента.
     * @param patronymic  Отчество студента.
     * @param group       Группа студента.
     */
    public Student(String lastName, String firstName, String patronymic, String group) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.group = group;
        this.fullName = getFullName(); // Формирование полного имени
    }

    /**
     * Конструктор для создания студента, указывая только полное имя и группу.
     *
     * @param fullName Полное имя студента.
     * @param group    Группа студента.
     */
    public Student(String fullName, String group) {
        this.fullName = fullName;
        this.group = group;
    }

    /**
     * Метод для очистки всех данных о посещениях и отсутствиях.
     */
    public void clearAttendance() {
        attendanceDates.clear();
        absenceDates.clear();
    }

    /**
     * Получить полное имя студента.
     * Если оно уже задано, возвращает его. В противном случае собирает его из фамилии, имени и отчества.
     *
     * @return Полное имя студента.
     */
    public String getFullName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        return (lastName == null ? "" : lastName) + " " +
                (firstName == null ? "" : firstName) + " " +
                (patronymic == null ? "" : patronymic).trim();
    }

    // Методы доступа

    /**
     * Получить фамилию студента.
     *
     * @return Фамилия студента.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Получить имя студента.
     *
     * @return Имя студента.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Получить отчество студента.
     *
     * @return Отчество студента.
     */
    public String getPatronymic() {
        return patronymic;
    }

    /**
     * Получить группу студента.
     *
     * @return Группа студента.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Установить новую группу для студента.
     *
     * @param group Новая группа.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Получить все даты посещений студента.
     *
     * @return Множество дат посещений.
     */
    public Set<String> getAttendanceDates() {
        return attendanceDates;
    }

    /**
     * Получить все даты отсутствий студента.
     *
     * @return Множество дат отсутствий.
     */
    public Set<String> getAbsenceDates() {
        return absenceDates;
    }

    /**
     * Добавить дату посещения студента.
     * Если студент был ранее отмечен как отсутствующий в этот день, дата будет удалена из списка отсутствий.
     *
     * @param date Дата посещения.
     */
    public void addAttendanceDate(String date) {
        attendanceDates.add(date);
        absenceDates.remove(date); // Удалить из отсутствий, если есть
    }

    /**
     * Добавить дату отсутствия студента.
     * Если студент был ранее отмечен как посещающий в этот день, дата будет удалена из списка посещений.
     *
     * @param date Дата отсутствия.
     */
    public void addAbsenceDate(String date) {
        absenceDates.add(date);
        attendanceDates.remove(date); // Удалить из посещений, если есть
    }

    /**
     * Переопределение метода toString для удобного вывода информации о студенте.
     * Возвращает строку с полным именем, группой, датами посещений и отсутствий.
     *
     * @return Строка с информацией о студенте.
     */
    @Override
    public String toString() {
        String attendanceStr = attendanceDates.isEmpty() ? "Нет посещений" : String.join(", ", attendanceDates);
        String absenceStr = absenceDates.isEmpty() ? "Нет отсутствий" : String.join(", ", absenceDates);
        return String.format("ФИО: %s, Группа: %s, Посещения: %s, Отсутствия: %s",
                getFullName(), group, attendanceStr, absenceStr);
    }
}
