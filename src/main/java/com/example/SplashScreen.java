package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Класс SplashScreen представляет собой окно загрузки (splash screen),
 * которое отображает информацию о проекте и предоставляет пользователю
 * возможность продолжить или выйти.
 *
 * @author Soldatov N. V.
 * @version 22.0.2
 */
public class SplashScreen extends JFrame {

    /**
     * Поле, указывающее, была ли нажата кнопка продолжения.
     */
    private boolean isContinuePressed = false;

    /**
     * Таймер для отслеживания времени на экране.
     */
    private Timer timer;

    /**
     * Конструктор, создающий окно splash screen.
     * Конфигурирует все элементы UI и их взаимодействие.
     */
    public SplashScreen() {
        // Настройка заголовка окна
        setTitle("SplashScreen");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Основная панель
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Панель с текстом
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        // Текстовые метки
        JLabel universityLabel = new JLabel("БЕЛОРУССКИЙ НАЦИОНАЛЬНЫЙ ТЕХНИЧЕСКИЙ УНИВЕРСИТЕТ");
        universityLabel.setFont(new Font("Arial", Font.BOLD, 20));
        universityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel facultyLabel = new JLabel("Факультет информационных технологий и робототехники");
        facultyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        facultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel departmentLabel = new JLabel("Кафедра программного обеспечения информационных систем и технологий");
        departmentLabel.setFont(new Font("Arial", Font.BOLD, 16));
        departmentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel courseLabel = new JLabel("КУРСОВАЯ РАБОТА");
        courseLabel.setFont(new Font("Arial", Font.BOLD, 24));
        courseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel disciplineLabel = new JLabel("по дисциплине \"Программирование на языке Java\"");
        disciplineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        disciplineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel topicLabel = new JLabel("ПОСЕЩАЕМОСТЬ ЛЕКЦИОННЫХ ЗАНЯТИЙ");
        topicLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Добавление меток в текстовую панель
        textPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textPanel.add(universityLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textPanel.add(facultyLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(departmentLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        textPanel.add(courseLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(disciplineLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textPanel.add(topicLabel);

        // Нижняя панель с фото, автором и преподавателем
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);

        // Фото
        JLabel photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(200, 200));
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        photoLabel.setVerticalAlignment(JLabel.CENTER);

        // Загрузка изображения
        ImageIcon photoIcon = null;
        try {
            java.net.URL imgURL = getClass().getResource("/photo_splash.png");
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage();
                photoIcon = new ImageIcon(img.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            } else {
                throw new NullPointerException("Файл изображения /photo_splash.png не найден.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки изображения: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        // Установка изображения, если оно загружено
        if (photoIcon != null) {
            photoLabel.setIcon(photoIcon);
        }

        // Панель для фото
        JPanel photoContainer = new JPanel();
        photoContainer.setLayout(new BorderLayout());
        photoContainer.setBackground(Color.WHITE);
        photoContainer.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 0));
        photoContainer.add(photoLabel, BorderLayout.CENTER);

        // Панель с текстами (автор, преподаватель)
        JPanel textAlignmentPanel = new JPanel();
        GroupLayout layout = new GroupLayout(textAlignmentPanel);
        textAlignmentPanel.setLayout(layout);
        textAlignmentPanel.setBackground(Color.WHITE);

        JLabel authorLabel = new JLabel("Выполнил: Студент группы 10702122");
        JLabel fioLabel = new JLabel("Солдатов Никита Викторович");
        JLabel supervisorLabel = new JLabel("Преподаватель: к.ф.-м.н., доц.");
        JLabel supervisorNameLabel = new JLabel("Сидорик Валерий Владимирович");

        // Установка шрифтов для текста
        authorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        fioLabel.setFont(new Font("Arial", Font.BOLD, 14));
        supervisorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        supervisorNameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Компоновка текста
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGap(200)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(authorLabel)
                                .addComponent(fioLabel)
                                .addComponent(supervisorLabel)
                                .addComponent(supervisorNameLabel))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(80)
                        .addComponent(authorLabel)
                        .addComponent(fioLabel)
                        .addGap(20)
                        .addComponent(supervisorLabel)
                        .addComponent(supervisorNameLabel)
        );

        // Город и год
        JLabel cityLabel = new JLabel("Минск, 2024");
        cityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cityLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Компоновка нижней панели
        bottomPanel.add(photoContainer, BorderLayout.WEST);
        bottomPanel.add(textAlignmentPanel, BorderLayout.CENTER);
        bottomPanel.add(cityLabel, BorderLayout.SOUTH);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        // Кнопки "Выйти" и "Далее"
        JButton exitButton = new JButton("Выйти");
        JButton continueButton = new JButton("Далее");
        buttonPanel.add(continueButton);
        buttonPanel.add(exitButton);

        // Действия на кнопки
        exitButton.addActionListener(e -> {
            timer.stop();
            System.exit(0);
        });

        continueButton.addActionListener(e -> {
            timer.stop();
            this.dispose();
            new MainFrame().setVisible(true);
        });

        // Таймер для автоматического закрытия
        timer = new Timer(60000, (ActionEvent e) -> {
            if (this.isVisible()) {
                this.dispose();
                System.exit(0);
            }
        });
        timer.setRepeats(false);
        timer.start();

        // Добавление всех панелей в главное окно
        mainPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Основной метод для запуска приложения и отображения splash screen.
     *
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SplashScreen().setVisible(true));
    }
}
