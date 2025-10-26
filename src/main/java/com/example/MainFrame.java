package com.example;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import com.toedter.calendar.JCalendar;



/**
 * Главный класс приложения для учета посещаемости студентов.
 * Этот класс расширяет {@link JFrame} и реализует графический интерфейс
 * для добавления, отображения и фильтрации информации о студентах и их посещаемости.
 *
 * Включает функционал:
 * <ul>
 *     <li>Добавление посещений студентов с указанием ФИО, группы и даты посещения;</li>
 *     <li>Отображение таблицы студентов;</li>
 *     <li>Фильтрация студентов по различным критериям (дата, группа, ФИО);</li>
 *     <li>Экспорт и импорт данных из Excel;</li>
 *     <li>Отправка обратной связи по email.</li>
 * </ul>
 *
 * @author Soldatov N. V.
 * @version 22.0.2
 */ 
public class MainFrame extends JFrame {

    /**
     * Список студентов, где ключ - это ФИО студента, а значение - объект {@link Student}.
     */
    private Map<String, Student> students = new HashMap<>();

    /**
     * Область для отображения информации о текущем состоянии.
     */
    private JTextArea displayArea;

    /**
     * Поля для ввода информации о студенте.
     */
    private JTextField nameField, groupField, dateField;

    /**
     * Таблица для отображения списка студентов.
     */
    private JTable table;

    /**
     * Модель таблицы для отображения данных студентов.
     */
    private DefaultTableModel tableModel;

    /**
     * Комбинированное поле для выбора фильтра.
     */
    private JComboBox<String> filterComboBox;

    /**
     * Поле ввода для ввода данных фильтрации.
     */
    private JTextField filterInputField;

    /**
     * Отправитель email для обратной связи.
     */
    private final EmailSender emailSender;

    /**
     * Электронная почта получателя для обратной связи.
     */
    private final String recipientEmail = "miracleqxz@gmail.com";

    /**
     * Комбинированное поле для выбора месяца.
     */
    private JComboBox<String> monthComboBox;

    /**
     * Текущий выбранный месяц для отображения посещаемости студентов.
     */
    private LocalDate selectedMonth = LocalDate.now().withDayOfMonth(1);

    /**
     * Конструктор, инициализирующий интерфейс, компоненты и действия.
     */
    public MainFrame() {
        setTitle("Учет посещаемости студентов");
        setSize(800, 600);  // Уменьшенный размер окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Панель ввода с более компактным GridLayout
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5)); // 4 строки, 2 столбца
        inputPanel.setBorder(BorderFactory.createTitledBorder("Добавить студента"));

        // Поле ввода ФИО
        inputPanel.add(new JLabel("ФИО:"));
        nameField = new JTextField(15);
        inputPanel.add(nameField);

        // Поле ввода группы
        inputPanel.add(new JLabel("Группа:"));
        groupField = new JTextField(15);
        inputPanel.add(groupField);

        // Поле ввода даты
        inputPanel.add(new JLabel("Дата посещения (гггг-мм-дд):"));
        dateField = new JTextField(15);
        inputPanel.add(dateField);

        // Кнопка добавления
        JButton addButton = new JButton("Добавить посещение");
        addButton.addActionListener(this::addAttendance);
        inputPanel.add(addButton);

        // Выбор месяца
        inputPanel.add(new JLabel("Выберите месяц:"));
        String[] months = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(selectedMonth.getMonthValue() - 1);
        monthComboBox.addActionListener(e -> {
            selectedMonth = LocalDate.now().withMonth(monthComboBox.getSelectedIndex() + 1).withDayOfMonth(1);
            updateTableForMonth(selectedMonth); // Обновляем таблицу
        });
        inputPanel.add(monthComboBox);

        // Инициализация таблицы
        String[] columnNames = generateColumnNames(selectedMonth);
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table);

        // Панель кнопок с уменьшенными кнопками
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5)); // Изменено с 3 на 4 для нового места
        JButton saveExcelButton = new JButton("Сохранить в Excel");
        saveExcelButton.addActionListener(e -> saveToExcel());
        buttonPanel.add(saveExcelButton);

        JButton loadExcelButton = new JButton("Загрузить из Excel");
        loadExcelButton.addActionListener(e -> loadFromExcel());
        buttonPanel.add(loadExcelButton);

        JButton clearButton = new JButton("Очистить всех студентов");
        clearButton.addActionListener(e -> {
            students.clear();
            updateDisplayArea();
        });
        buttonPanel.add(clearButton);

        // Добавляем новую кнопку для открытия карты студентов
        JButton openStudentMapButton = new JButton("Открыть карту студентов");
        openStudentMapButton.addActionListener(e -> {
            String subject = JOptionPane.showInputDialog(this, "Введите предмет:");
            if (subject != null && !subject.trim().isEmpty()) {
                openStudentMap(subject); // Вызов ранее реализованного метода
            } else {
                JOptionPane.showMessageDialog(this, "Предмет не указан!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(openStudentMapButton);

        // Панель фильтрации
        JPanel filterPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        String[] filterOptions = {"Выберите фильтрацию", "Фильтровать по дате", "Фильтровать по группе", "Фильтровать по ФИО"};
        filterComboBox = new JComboBox<>(filterOptions);
        filterComboBox.addActionListener(e -> updateFilterFields());
        filterPanel.add(new JLabel("Выберите метод фильтрации:"));
        filterPanel.add(filterComboBox);

        filterInputField = new JTextField(15);
        filterPanel.add(new JLabel("Введите данные для фильтрации:"));
        filterPanel.add(filterInputField);

        // Кнопка выполнения фильтрации
        JButton applyFilterButton = new JButton("Выполнить");
        applyFilterButton.addActionListener(e -> {
            String selectedFilter = (String) filterComboBox.getSelectedItem();
            String input = filterInputField.getText().trim();
            applyFilter(selectedFilter, input);
        });
        filterPanel.add(applyFilterButton);

        // Добавляем кнопку "Сбросить фильтры"
        JButton resetFiltersButton = new JButton("Сбросить фильтры");
        resetFiltersButton.addActionListener(e -> resetFilters());// Возвращаем исходные данные
        filterPanel.add(resetFiltersButton);

        // Основная панель
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(filterPanel, BorderLayout.SOUTH);

        // Создаем меню
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Меню "Файл" с всплывающим меню
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Добавляем пункт меню "Exit" в меню "File"
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            System.exit(0); // Завершаем программу
        });
        fileMenu.add(exitItem);

        // Меню "Информация"
        JMenu infoMenu = new JMenu("Information");
        menuBar.add(infoMenu);

        // Пункт меню "О программе"
        JMenuItem aboutProgramItem = new JMenuItem("О программе");
        aboutProgramItem.addActionListener(e -> showAboutProgram());
        infoMenu.add(aboutProgramItem);

        // Пункт меню "Об авторе"
        JMenuItem aboutAuthorItem = new JMenuItem("Об авторе");
        aboutAuthorItem.addActionListener(e -> showAboutAuthor());
        infoMenu.add(aboutAuthorItem);

        // Меню "Помощь" (пока пустое)
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        // Пункт меню для отправки письма
        JMenuItem sendFeedbackMenuItem = new JMenuItem("Send Feedback");
            emailSender = new EmailSender("nikitikk5@yandex.ru", "muvnquyaqebtolhk", "smtp.yandex.ru", 465, recipientEmail);
        sendFeedbackMenuItem.addActionListener(this::onSendFeedback);
        helpMenu.add(sendFeedbackMenuItem);

        add(mainPanel);
        updateDisplayArea();
    }


    /**
     * Обрабатывает событие для отправки отзыва.
     * Создает и отображает окно для ввода письма с полями "Тема" и "Сообщение".
     * При отправке письма использует {@link EmailSender} для отправки.
     * Закрывает окно после отправки.
     *
     * @param e Событие, вызвавшее обработчик
     */
    private void onSendFeedback(ActionEvent e) {
        // Создаем и показываем окно для ввода письма
        JFrame frame = new JFrame("Send Feedback");
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout(10, 10)); // Добавляем отступы
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Закрытие только окна

        // Поля ввода
        JTextField subjectField = new JTextField();
        JTextArea messageArea = new JTextArea();
        JButton sendButton = new JButton("Отправить");

        // Панель ввода
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Отступы между элементами
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Тема:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(subjectField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(new JLabel("Сообщение:"), gbc);

        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane scrollPane = new JScrollPane(messageArea);
        inputPanel.add(scrollPane, gbc);

        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(sendButton, BorderLayout.SOUTH);

        // Кнопка отправки
        sendButton.addActionListener((ActionEvent event) -> {
            String subject = subjectField.getText();
            String text = messageArea.getText();
            emailSender.sendEmail(subject, text, this);
            frame.dispose(); // Закрываем окно после отправки
        });

        frame.setLocationRelativeTo(null); // Центрируем окно
        frame.setVisible(true);
    }

    /**
     * Обновляет доступность полей ввода в зависимости от выбранного фильтра.
     * Включает соответствующие подсказки и очищает поле ввода, если это необходимо.
     *
     */
    private void updateFilterFields() {
        String selectedFilter = (String) filterComboBox.getSelectedItem();
        if ("Фильтровать по ФИО".equals(selectedFilter)) {
            filterInputField.setEnabled(true);
            filterInputField.setText("");
            filterInputField.setToolTipText("Введите фамилию студента для фильтрации");
        } else if ("Фильтровать по группе".equals(selectedFilter)) {
            filterInputField.setEnabled(true);
            filterInputField.setText("");
            filterInputField.setToolTipText("Введите группу студента для фильтрации");
        } else if ("Фильтровать по дате".equals(selectedFilter)) {
            filterInputField.setEnabled(true);
            filterInputField.setText("");
            filterInputField.setToolTipText("Введите дату для фильтрации");
        } else {
            filterInputField.setEnabled(false);
            filterInputField.setText("");
        }
    }

    /**
     * Добавляет посещение для выбранного студента.
     * Проверяет корректность ввода и формат даты.
     * После добавления обновляет таблицу с посещаемостью.
     *
     * @param e Событие, вызвавшее обработчик
     */
    private void addAttendance(ActionEvent e) {
        try {
            String fullName = nameField.getText().trim();
            String group = groupField.getText().trim();
            String date = dateField.getText().trim();

            // Проверка на пустые поля
            if (fullName.isEmpty() || group.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Все поля должны быть заполнены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Валидация ФИО (Фамилия Имя Отчество)
            if (!fullName.matches("[А-Яа-яЁёA-Za-z]+\\s[А-Яа-яЁёA-Za-z]+\\s[А-Яа-яЁёA-Za-z]+")) {
                JOptionPane.showMessageDialog(this, "ФИО должно быть в формате: Фамилия Имя Отчество, без лишних пробелов и специальных символов!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Валидация номера группы (8 цифр)
            if (!group.matches("\\d{8}")) {
                JOptionPane.showMessageDialog(this, "Номер группы должен содержать ровно 8 цифр!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Валидация даты
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);

            try {
                Date parsedDate = sdf.parse(date);

                Calendar cal = Calendar.getInstance();
                cal.setTime(parsedDate);

                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1; // Январь = 0
                int currentYear = LocalDate.now().getYear();

                // Проверка года и месяца
                if (year > currentYear) {
                    JOptionPane.showMessageDialog(this, "Год не может быть больше текущего!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (month < 1 || month > 12) {
                    JOptionPane.showMessageDialog(this, "Месяц должен быть в диапазоне от 1 до 12!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Некорректный формат даты! Используйте формат yyyy-MM-dd.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Если все проверки пройдены, добавляем студента
            Student student = students.getOrDefault(fullName, new Student(fullName, "", "", group));
            student.addAttendanceDate(date);
            students.put(fullName, student);

            updateDisplayArea(); // Обновляем таблицу после добавления
            nameField.setText("");
            groupField.setText("");
            dateField.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка ввода данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }



    /**
     * Обновляет таблицу с посещаемостью студентов.
     * Для каждого студента рассчитывается количество посещений в выбранный месяц.
     * Вставляются значения в таблицу, включая посещения и отсутствие.
     */
    private void updateDisplayArea() {
        tableModel.setRowCount(0); // Очистить текущую таблицу
        for (Student student : students.values()) {
            List<String> row = new ArrayList<>();
            row.add(student.getFullName());
            row.add(student.getGroup());

            int attendanceCount = 0; // Счётчик посещений

            LocalDate startOfMonth = selectedMonth.withDayOfMonth(1);
            LocalDate endOfMonth = selectedMonth.withDayOfMonth(selectedMonth.lengthOfMonth());

            for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
                String formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (student.getAttendanceDates().contains(formattedDate)) {
                    row.add("✓");
                    attendanceCount++; // Увеличиваем счётчик посещений
                } else if (student.getAbsenceDates().contains(formattedDate)) {
                    row.add("✗");
                } else {
                    row.add("");
                }
            }

            // Добавляем количество посещений в строку
            row.add(2, String.valueOf(attendanceCount)); // Вставляем в колонку "Посещения"
            tableModel.addRow(row.toArray());
        }

        // Применение рендера после обновления данных
        for (int i = 3; i < tableModel.getColumnCount(); i++) { // Колонки с датами начинаются с 3
            table.getColumnModel().getColumn(i).setCellRenderer(getCustomTableCellRenderer());
        }
    }

    /**
     * Применяет фильтр к данным в таблице на основе выбранного типа фильтра и введенного значения.
     * Сохраняет текущее состояние перед фильтрацией, очищает таблицу и добавляет только отфильтрованные данные.
     *
     * @param filterType Тип фильтра
     * @param input Ввод пользователя для фильтрации
     */
    private void applyFilter(String filterType, String input) {
        saveTableStateToStudents(); // Сохраняем текущее состояние перед фильтрацией

        tableModel.setRowCount(0); // Очищаем таблицу
        for (Student student : students.values()) {
            boolean matches = false;

            if ("Фильтровать по дате".equals(filterType)) {
                matches = student.getAttendanceDates().contains(input);
            } else if ("Фильтровать по группе".equals(filterType)) {
                matches = student.getGroup().equalsIgnoreCase(input);
            } else if ("Фильтровать по ФИО".equals(filterType)) {
                matches = student.getFullName().toLowerCase().contains(input.toLowerCase());
            }

            if (matches) {
                addStudentRowToTable(student); // Добавляем отфильтрованного студента
            }
        }
    }

    /**
     * Сохраняет данные таблицы в Excel файл.
     * Пользователь выбирает место для сохранения файла, и данные сохраняются в формате XLSX.
     */
    private void saveToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить в Excel");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(filePath)) {
                Sheet sheet = workbook.createSheet("Attendance");

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    headerRow.createCell(i).setCellValue(tableModel.getColumnName(i));
                }

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        row.createCell(j).setCellValue(String.valueOf(tableModel.getValueAt(i, j)));
                    }
                }

                workbook.write(fileOut);
                JOptionPane.showMessageDialog(this, "Данные успешно сохранены в Excel!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка при сохранении файла: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Загружает данные студентов из файла Excel.
     * <p>
     * Этот метод предоставляет возможность выбрать файл Excel, содержащий информацию о студентах.
     * Данные из файла загружаются в текущий список студентов. Пропускаются строки с пустыми значениями
     * или некорректными ФИО. В случае ошибок при загрузке данных, пользователю показывается сообщение об ошибке.
     * </p>
     */
    private void loadFromExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Загрузить из Excel");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileInputStream fis = new FileInputStream(fileChooser.getSelectedFile());
                 Workbook workbook = WorkbookFactory.create(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                students.clear(); // Очищаем текущий список студентов

                Iterator<Row> rowIterator = sheet.rowIterator();
                if (rowIterator.hasNext()) rowIterator.next(); // Пропускаем заголовок

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

                    String fullName = getCellValueAsString(row.getCell(0));
                    String group = getCellValueAsString(row.getCell(1));

                    if (fullName == null || fullName.isEmpty() || group == null || group.isEmpty()) {
                        continue; // Пропуск пустых строк
                    }

                    // Удаление лишних пробелов
                    fullName = fullName.trim();
                    group = group.trim();

                    // Разделение ФИО на части
                    String[] nameParts = fullName.split("\\s+");
                    if (nameParts.length < 2) {
                        JOptionPane.showMessageDialog(this, "Некорректное ФИО: " + fullName, "Ошибка", JOptionPane.ERROR_MESSAGE);
                        continue; // Пропуск некорректных строк
                    }

                    String lastName = nameParts[0];
                    String firstName = nameParts.length > 1 ? nameParts[1] : "";
                    String patronymic = nameParts.length > 2 ? nameParts[2] : "";

                    // Создаем объект студента
                    Student student = new Student(lastName, firstName, patronymic, group);

                    // Добавляем студента в список
                    students.put(fullName, student);
                }

                updateDisplayArea(); // Обновляем таблицу
                JOptionPane.showMessageDialog(this, "Данные успешно загружены!", "Успех", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Получает значение ячейки Excel как строку.
     * <p>
     * Этот метод извлекает значение из ячейки Excel в виде строки, учитывая тип данных в ячейке.
     * Если ячейка содержит число, оно конвертируется в строку. Если ячейка содержит дату, дата будет
     * преобразована в строковое представление. Если ячейка пуста или имеет неподдерживаемый тип, метод возвращает пустую строку.
     * </p>
     *
     * @param cell ячейка Excel, из которой необходимо извлечь значение.
     * @return строковое представление значения ячейки.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue()); // Если число, то приводим к строке
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * Сбрасывает фильтры и восстанавливает состояние таблицы.
     * <p>
     * Этот метод сохраняет текущее состояние таблицы в список студентов перед сбросом фильтров.
     * После сброса фильтров, отображение обновляется для отображения всех студентов.
     * </p>
     */
    private void resetFilters() {
        saveTableStateToStudents(); // Сохраняем текущее состояние перед сбросом фильтров
        updateDisplayArea();
    }


    /**
     * Отображает окно с информацией о программе.
     * <p>
     * Метод создает и отображает окно с информацией о программе, включая описание функциональности,
     * фото и текст. Также имеется кнопка "Выход" для закрытия окна.
     * </p>
     */
    private void showAboutProgram() {
        JDialog aboutDialog = new JDialog(this, "О программе", true);
        aboutDialog.setSize(610, 350); // Размер окна, как на скриншоте
        aboutDialog.setLayout(new BorderLayout());

        // Панель для размещения картинки и текста
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Фото
        JLabel photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(200, 200));
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        photoLabel.setVerticalAlignment(JLabel.CENTER);

        ImageIcon photoIcon = null;
        try {
            java.net.URL imgURL = getClass().getResource("/photo10.jpg"); // Путь к изображению в ресурсах
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage();
                photoIcon = new ImageIcon(img.getScaledInstance(400, 300, Image.SCALE_SMOOTH));
            } else {
                throw new NullPointerException("Файл изображения /photo10.jpg не найден.");
            }
        } catch (Exception ex) {
            String errorMessage = "Ошибка загрузки изображения: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage, "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.err.println(errorMessage);
        }

        if (photoIcon != null) {
            photoLabel.setIcon(photoIcon);
        }

        contentPanel.add(photoLabel, BorderLayout.WEST);

        // Текстовая область
        JTextArea aboutTextArea = new JTextArea();
        aboutTextArea.setEditable(false);
        aboutTextArea.setText(
                "Программа учета посещаемости студентов\n\n" + // Новое описание
                        "Программа позволяет:\n" +
                        "1. Добавлять посещения студентов с указанием ФИО, группы и даты.\n" +
                        "2. Сохранять данные о посещаемости в Excel файл.\n" +
                        "3. Загружать данные о посещаемости из Excel файла.\n" +
                        "4. Фильтровать студентов по ФИО, группе или дате посещения.\n" +
                        "5. Очищать все данные о посещаемости."
        );
        aboutTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Отступы от краев
        contentPanel.add(aboutTextArea, BorderLayout.CENTER);


        aboutDialog.add(contentPanel, BorderLayout.CENTER);


        // Панель для версии и кнопки
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JLabel versionLabel = new JLabel("Версия ver. 23.0.1.2024");
        bottomPanel.add(versionLabel, BorderLayout.WEST);


        JButton closeButton = new JButton("Выход");
        closeButton.setBackground(new Color(255, 102, 102)); // Розовый цвет кнопки
        closeButton.addActionListener(e -> aboutDialog.dispose());
        bottomPanel.add(closeButton, BorderLayout.EAST);


        aboutDialog.add(bottomPanel, BorderLayout.SOUTH);


        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }

    /**
     * Отображает окно с информацией об авторе программы.
     * <p>
     * Этот метод создает окно с информацией об авторе, включая фото и контактные данные.
     * Также есть кнопка "Назад" для закрытия окна.
     * </p>
     */
    private void showAboutAuthor() {
        JDialog authorDialog = new JDialog(this, "Об авторе", true);
        authorDialog.setSize(700, 700);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Фото
        JLabel photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(200, 200));
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        photoLabel.setVerticalAlignment(JLabel.CENTER);

        ImageIcon photoIcon = null;
        try {
            java.net.URL imgURL = getClass().getResource("/photo_about_author.jpg"); // Путь к изображению в ресурсах
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage();
                photoIcon = new ImageIcon(img.getScaledInstance(600, 500, Image.SCALE_SMOOTH));
            } else {
                throw new NullPointerException("Файл изображения /photo2.jpg не найден.");
            }
        } catch (Exception ex) {
            String errorMessage = "Ошибка загрузки изображения: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage, "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.err.println(errorMessage);
        }

        if (photoIcon != null) {
            photoLabel.setIcon(photoIcon);
        }

        mainPanel.add(photoLabel, BorderLayout.CENTER); // Добавляем фото в центр

        // Текстовая информация об авторе
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Отступы

        JLabel authorLabel = new JLabel("Автор");
        JLabel groupLabel = new JLabel("Студент группы 10702122");
        JLabel nameLabel = new JLabel("Солдатов Никита Викторович");
        JLabel emailLabel = new JLabel("miracleqxz@gmail.com");

        // Установка шрифта и выравнивания для всех меток
        Font boldFont = new Font("Arial", Font.BOLD, 16);
        authorLabel.setFont(boldFont);
        groupLabel.setFont(boldFont);
        nameLabel.setFont(boldFont);
        emailLabel.setFont(boldFont);

        // Центрирование текста
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        groupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Добавляем метки в панель
        textPanel.add(authorLabel);
        textPanel.add(Box.createVerticalStrut(10)); // Отступ между строками
        textPanel.add(groupLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(emailLabel);

        mainPanel.add(textPanel, BorderLayout.SOUTH);

        // Кнопка "Назад"
        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> authorDialog.dispose());
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backButtonPanel.add(backButton);

        // Добавляем все в диалог
        authorDialog.add(mainPanel, BorderLayout.CENTER);
        authorDialog.add(backButtonPanel, BorderLayout.SOUTH);

        authorDialog.setLocationRelativeTo(this);
        authorDialog.setVisible(true);
    }

    /**
     * Открывает карту студента для выбора даты посещения и отметки присутствия.
     * <p>
     * Метод отображает окно с картой студента, где можно выбрать дату и отметить присутствие студента.
     * Также показывается информация о студенте, и пользователь может отмечать студентов как присутствующих
     * или отсутствующих на определенные даты.
     * </p>
     *
     * @param subject название предмета, который отображается в диалоговом окне.
     */
    private void openStudentMap(String subject) {
        JDialog studentMapDialog = new JDialog(this, "Карта студента", true);
        studentMapDialog.setSize(600, 500);
        studentMapDialog.setLayout(new BorderLayout(10, 10));

        // Панель информации о студенте
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация о студенте"));

        JLabel nameLabel = new JLabel("ФИО: ");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel groupLabel = new JLabel("Группа: ");
        groupLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel subjectLabel = new JLabel("Предмет: " + subject);
        subjectLabel.setFont(new Font("Arial", Font.ITALIC, 14));

        JPanel labelsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        labelsPanel.add(nameLabel);
        labelsPanel.add(groupLabel);
        labelsPanel.add(subjectLabel);

        infoPanel.add(labelsPanel, BorderLayout.CENTER);
        studentMapDialog.add(infoPanel, BorderLayout.NORTH);

        // Календарь для выбора даты
        JCalendar calendar = new JCalendar();
        calendar.setBorder(BorderFactory.createTitledBorder("Выберите дату"));
        studentMapDialog.add(calendar, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton presentButton = new JButton("Присутствовал");
        JButton absentButton = new JButton("Отсутствовал");
        JButton closeButton = new JButton("Закрыть");

        presentButton.setBackground(new Color(102, 255, 102));
        presentButton.setForeground(Color.WHITE);
        absentButton.setBackground(new Color(255, 102, 102));
        absentButton.setForeground(Color.WHITE);
        closeButton.setBackground(Color.LIGHT_GRAY);

        buttonPanel.add(presentButton);
        buttonPanel.add(absentButton);
        buttonPanel.add(closeButton);

        studentMapDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Список студентов
        List<Student> studentList = new ArrayList<>(students.values());
        if (studentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Список студентов пуст!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int[] currentIndex = {0};

        // Метод для обновления информации о текущем студенте
        Runnable updateStudentInfo = () -> {
            if (currentIndex[0] < studentList.size()) {
                Student currentStudent = studentList.get(currentIndex[0]);
                nameLabel.setText("ФИО: " + currentStudent.getFullName());
                groupLabel.setText("Группа: " + currentStudent.getGroup());
            } else {
                JOptionPane.showMessageDialog(studentMapDialog, "Все студенты обработаны!", "Завершено", JOptionPane.INFORMATION_MESSAGE);
                studentMapDialog.dispose();
            }
        };

        updateStudentInfo.run();

        // Обработчики кнопок
        ActionListener markAttendance = e -> {
            if (currentIndex[0] < studentList.size()) {
                Student currentStudent = studentList.get(currentIndex[0]);
                String selectedDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getDate());

                if (e.getSource() == presentButton) {
                    currentStudent.addAttendanceDate(selectedDate);
                    JOptionPane.showMessageDialog(studentMapDialog, "Отмечено 'Присутствовал' для даты " + selectedDate, "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else if (e.getSource() == absentButton) {
                    currentStudent.addAbsenceDate(selectedDate);
                    JOptionPane.showMessageDialog(studentMapDialog, "Отмечено 'Отсутствовал' для даты " + selectedDate, "Успех", JOptionPane.INFORMATION_MESSAGE);
                }

                currentIndex[0]++;
                updateStudentInfo.run();
                updateDisplayArea(); // Обновляем таблицу в главном окне
            }
        };

        presentButton.addActionListener(markAttendance);
        absentButton.addActionListener(markAttendance);

        closeButton.addActionListener(e -> studentMapDialog.dispose());

        studentMapDialog.setLocationRelativeTo(this);
        studentMapDialog.setVisible(true);
    }




    // Метод для обновления таблицы
    private void updateTable(Student student, String mark) {
        String fullName = student.getFullName();
        int rowCount = tableModel.getRowCount();
        LocalDate today = LocalDate.now();
        String currentDateColumn = today.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"));

        for (int row = 0; row < rowCount; row++) {
            if (tableModel.getValueAt(row, 0).equals(fullName)) {
                int colCount = tableModel.getColumnCount();
                for (int col = 2; col < colCount; col++) {
                    if (tableModel.getColumnName(col).equals(currentDateColumn)) {
                        tableModel.setValueAt(mark, row, col);

                        // Обновляем данные в объекте Student
                        if (mark.equals("✓")) {
                            student.addAttendanceDate(today.toString());
                        } else if (mark.equals("✗")) {
                            student.addAbsenceDate(today.toString());
                        }

                        return; // Обновляем только одну строку
                    }
                }
            }
        }
    }




/*
    private void openStudentMap(String subject) {
        JDialog studentMapDialog = new JDialog(this, "Карта студентов", true);
        studentMapDialog.setSize(500, 400);
        studentMapDialog.setLayout(new BorderLayout());

        // Панель для отображения текущего студента
        JPanel studentInfoPanel = new JPanel(new GridLayout(5, 1));
        JLabel studentNameLabel = new JLabel("ФИО: ");
        JLabel studentGroupLabel = new JLabel("Группа:   ");
        JLabel datesLabel = new JLabel("Даты посещений: ");
        JLabel subjectLabel = new JLabel("Предмет: " + subject);

        studentInfoPanel.add(studentNameLabel);
        studentInfoPanel.add(studentGroupLabel);
        studentInfoPanel.add(datesLabel);
        studentInfoPanel.add(subjectLabel);

        studentMapDialog.add(studentInfoPanel, BorderLayout.NORTH);

        // Календарь для выбора даты
        com.toedter.calendar.JCalendar calendar = new com.toedter.calendar.JCalendar();
        studentMapDialog.add(calendar, BorderLayout.CENTER);

        // Панель для кнопок управления
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton presentButton = new JButton("Присутствовал");
        JButton absentButton = new JButton("Отсутствовал");
        JButton nextStudentButton = new JButton("Следующий студент");
        buttonPanel.add(presentButton);
        buttonPanel.add(absentButton);
        buttonPanel.add(nextStudentButton);

        studentMapDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Логика работы с текущими студентами
        List<Student> studentList = new ArrayList<>(students.values());
        int[] currentIndex = {0};

        // Метод обновления информации о студенте
        Runnable updateStudentInfo = () -> {
            if (currentIndex[0] < studentList.size()) {
                Student student = studentList.get(currentIndex[0]);
                studentNameLabel.setText("ФИО: " + student.getFullName());
                studentGroupLabel.setText("Группа: " + student.getGroup());
                datesLabel.setText("Даты посещений: " + String.join(", ", student.getAttendanceDates()));
            } else {
                studentNameLabel.setText("ФИО: ");
                studentGroupLabel.setText("Группа: ");
                datesLabel.setText("Даты посещений: ");
            }
        };

        updateStudentInfo.run(); // Инициализация информации о первом студенте

        // Обработчик кнопки "Присутствовал"
        presentButton.addActionListener(e -> {
            if (currentIndex[0] < studentList.size()) {
                Student student = studentList.get(currentIndex[0]);
                Date selectedDate = calendar.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = sdf.format(selectedDate);

                student.addAttendanceDate(dateString); // Добавляем дату посещения
                students.put(student.getLastName(), student);

                updateDisplayArea(); // Обновляем главное окно
                updateStudentInfo.run();
                JOptionPane.showMessageDialog(studentMapDialog, "Дата " + dateString + " добавлена как 'Присутствовал'!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Обработчик кнопки "Отсутствовал"
        absentButton.addActionListener(e -> {
            if (currentIndex[0] < studentList.size()) {
                updateStudentInfo.run(); // Переход к следующему студенту
                JOptionPane.showMessageDialog(studentMapDialog, "Студент отмечен как 'Отсутствовал'!", "Информация", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Обработчик кнопки "Следующий студент"
        nextStudentButton.addActionListener(e -> {
            currentIndex[0]++;
            if (currentIndex[0] < studentList.size()) {
                updateStudentInfo.run();
            } else {
                JOptionPane.showMessageDialog(studentMapDialog, "Все студенты обработаны!", "Завершено", JOptionPane.INFORMATION_MESSAGE);
                studentMapDialog.dispose();
            }
        });

        studentMapDialog.setLocationRelativeTo(this);
        studentMapDialog.setVisible(true);
    }
*/

    /**
     * Генерирует массив названий колонок для таблицы, включая ФИО, группу, посещения и даты для выбранного месяца.
     *
     * @param month месяц, для которого генерируются названия колонок
     * @return массив строк, представляющих названия колонок
     */
    private String[] generateColumnNames(LocalDate month) {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("ФИО");
        columnNames.add("Группа");
        columnNames.add("Посещения"); // Новая колонка для подсчёта посещений

        LocalDate startOfMonth = month.withDayOfMonth(1);
        LocalDate endOfMonth = month.withDayOfMonth(month.lengthOfMonth());

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            columnNames.add(date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM")));
        }
        return columnNames.toArray(new String[0]);
    }

    /**
     * Добавляет строку с информацией о студенте в таблицу, включая ФИО, группу и отметки о посещениях за каждый день месяца.
     *
     * @param student студент, чьи данные необходимо добавить в таблицу
     */
    private void addStudentRowToTable(Student student) {
        List<String> row = new ArrayList<>();
        row.add(student.getFullName());
        row.add(student.getGroup());

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            String formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (student.getAttendanceDates().contains(formattedDate)) {
                row.add("✓");
            } else if (student.getAbsenceDates().contains(formattedDate)) {
                row.add("✗");
            } else {
                row.add("");
            }
        }

        tableModel.addRow(row.toArray());
    }

    /**
     * Сохраняет состояние таблицы в объектах студентов, обновляя их даты посещений и отсутствий.
     * Использует фамилию студента для поиска в списке студентов.
     */
    private void saveTableStateToStudents() {
        // Сохраняем состояние таблицы в объекте Student
        int rowCount = tableModel.getRowCount();
        int colCount = tableModel.getColumnCount();

        for (int row = 0; row < rowCount; row++) {
            String fullName = tableModel.getValueAt(row, 0).toString();
            Student student = students.get(fullName.split(" ")[0]); // Используем фамилию как ключ

            if (student != null) {
                LocalDate today = LocalDate.now();
                LocalDate startOfMonth = today.withDayOfMonth(1);

                // Очищаем старые данные и обновляем новые
                student.clearAttendance();

                for (int col = 2; col < colCount; col++) { // Колонки с датами начинаются с 2
                    String date = startOfMonth.plusDays(col - 2).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String mark = tableModel.getValueAt(row, col).toString();

                    if ("✓".equals(mark)) {
                        student.addAttendanceDate(date);
                    } else if ("✗".equals(mark)) {
                        student.addAbsenceDate(date);
                    }
                }
            }
        }
    }

    /**
     * Обновляет таблицу для выбранного месяца, сохраняя текущие данные и изменяя заголовки колонок.
     *
     * @param month месяц, для которого обновляется таблица
     */
    private void updateTableForMonth(LocalDate month) {
        saveTableStateToStudents(); // Сохраняем текущие данные
        tableModel.setColumnIdentifiers(generateColumnNames(month)); // Обновляем заголовки колонок
        updateDisplayArea();
    }

    /**
     * Возвращает кастомный рендерер для ячеек таблицы, который окрашивает ячейки в зависимости от отметок о посещении.
     *
     * @return рендерер для ячеек таблицы
     */
    private TableCellRenderer getCustomTableCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value != null) {
                    String cellValue = value.toString();
                    if ("✓".equals(cellValue)) {
                        cell.setBackground(new Color(102, 255, 102)); // Зеленый цвет
                        cell.setForeground(Color.BLACK);
                    } else if ("✗".equals(cellValue)) {
                        cell.setBackground(new Color(255, 102, 102)); // Красный цвет
                        cell.setForeground(Color.BLACK);
                    } else {
                        cell.setBackground(Color.WHITE); // Обычный фон для пустых ячеек
                        cell.setForeground(Color.BLACK);
                    }
                }
                return cell;
            }
        };
    }

    /**
     * Главный метод для запуска приложения, который создает и отображает окно.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
