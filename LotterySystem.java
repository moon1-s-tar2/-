import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class LotterySystem {
        private static final int TOTAL_NUMBERS = 36;
        private static final int SELECTED_NUMBERS = 7;
        private static final double TICKET_PRICE = 2.0;
        private static final String ADMIN_ID = "admin";
        private static final String ADMIN_PASSWORD = "admin123";

        private JFrame frame;
        private JPanel loginPanel, mainPanel, buyPanel, drawPanel, resultPanel;
        private JTextField userIdField, usernameField, passwordField, phoneField, amountField;
        private JPasswordField loginPasswordField;
        private JButton loginButton, registerButton, buyButton, drawButton, manualButton, randomButton;
        private JTextArea resultArea;
    private JLabel statusLabel;

    private Map<String, User> users = new HashMap<>();
    private Map<String, List<Ticket>> userTickets = new HashMap<>();
    private List<Integer> winningNumbers = new ArrayList<>();
    private boolean isDrawDone = false;
    private User currentUser = null;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LotterySystem window = new LotterySystem();
                // 取消注释此行以运行自动测试
                window.testLotterySystem();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LotterySystem() {
        initialize();
        // 添加管理员用户
        users.put(ADMIN_ID, new User(ADMIN_ID, "管理员", ADMIN_PASSWORD, 10000.0, "0"));
    }

    private void initialize() {
        frame = new JFrame("福利彩票36选7系统");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new CardLayout());

        // 初始化登录面板
        initLoginPanel();

        // 初始化主面板
        initMainPanel();

        // 初始化购买面板
        initBuyPanel();

        // 初始化抽奖面板
        initDrawPanel();

        // 初始化结果面板
        initResultPanel();

        // 添加所有面板到CardLayout
        frame.getContentPane().add(loginPanel, "login");
        frame.getContentPane().add(mainPanel, "main");
        frame.getContentPane().add(buyPanel, "buy");
        frame.getContentPane().add(drawPanel, "draw");
        frame.getContentPane().add(resultPanel, "result");
    }

    private void initLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(null);

        JLabel titleLabel = new JLabel("福利彩票36选7系统");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titleLabel.setBounds(300, 50, 250, 30);
        loginPanel.add(titleLabel);

        JLabel userIdLabel = new JLabel("用户ID:");
        userIdLabel.setBounds(250, 150, 80, 25);
        loginPanel.add(userIdLabel);

        userIdField = new JTextField();
        userIdField.setBounds(350, 150, 150, 25);
        loginPanel.add(userIdField);

        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(250, 200, 80, 25);
        loginPanel.add(passwordLabel);

        loginPasswordField = new JPasswordField();
        loginPasswordField.setBounds(350, 200, 150, 25);
        loginPanel.add(loginPasswordField);

        loginButton = new JButton("登录");
        loginButton.setBounds(280, 250, 100, 30);
        loginPanel.add(loginButton);

        registerButton = new JButton("注册");
        registerButton.setBounds(400, 250, 100, 30);
        loginPanel.add(registerButton);

        statusLabel = new JLabel("");
        statusLabel.setBounds(300, 300, 200, 25);
        loginPanel.add(statusLabel);

        // 登录按钮事件
        loginButton.addActionListener(e -> {
            String userId = userIdField.getText();
            String password = new String(loginPasswordField.getPassword());

            if (userId.isEmpty() || password.isEmpty()) {
                statusLabel.setText("请输入用户ID和密码");
                return;
            }

            User user = users.get(userId);
            if (user == null) {
                statusLabel.setText("用户不存在");
                return;
            }

            if (!user.getPassword().equals(password)) {
                statusLabel.setText("密码错误");
                return;
            }

            currentUser = user;
            statusLabel.setText("登录成功");
            updateMainPanel();
            CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
            cl.show(frame.getContentPane(), "main");

            // 检查是否中奖
            checkWinningStatus();
        });

        // 注册按钮事件
        registerButton.addActionListener(e -> {
            String userId = JOptionPane.showInputDialog(frame, "请输入用户ID:");
            if (userId == null || userId.isEmpty()) return;

            if (users.containsKey(userId)) {
                JOptionPane.showMessageDialog(frame, "用户ID已存在！");
                return;
            }

            String username = JOptionPane.showInputDialog(frame, "请输入用户名:");
            if (username == null || username.isEmpty()) return;

            String password = JOptionPane.showInputDialog(frame, "请输入密码:");
            if (password == null || password.isEmpty()) return;

            String phone = JOptionPane.showInputDialog(frame, "请输入电话号码:");
            if (phone == null || phone.isEmpty()) return;

            double amount = 0;
            try {
                String amountStr = JOptionPane.showInputDialog(frame, "请输入充值金额:");
                if (amountStr == null || amountStr.isEmpty()) return;
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(frame, "充值金额必须大于0！");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "金额格式错误！");
                return;
            }

            User newUser = new User(userId, username, password, amount, phone);
            users.put(userId, newUser);
            userTickets.put(userId, new ArrayList<>());
            JOptionPane.showMessageDialog(frame, "注册成功！");
        });
    }

    private void initMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(null);

        JLabel welcomeLabel = new JLabel("欢迎, ");
        welcomeLabel.setFont(new Font("宋体", Font.PLAIN, 18));
        welcomeLabel.setBounds(30, 30, 200, 30);
        mainPanel.add(welcomeLabel);

        JLabel balanceLabel = new JLabel("账户余额: ");
        balanceLabel.setBounds(30, 70, 200, 25);
        mainPanel.add(balanceLabel);

        JButton buyTicketButton = new JButton("购买彩票");
        buyTicketButton.setBounds(150, 150, 150, 40);
        mainPanel.add(buyTicketButton);

        JButton drawButton = new JButton("抽奖");
        drawButton.setBounds(350, 150, 150, 40);
        mainPanel.add(drawButton);

        JButton resultButton = new JButton("查看结果");
        resultButton.setBounds(550, 150, 150, 40);
        mainPanel.add(resultButton);

        JButton logoutButton = new JButton("退出登录");
        logoutButton.setBounds(650, 500, 100, 30);
        mainPanel.add(logoutButton);

        // 购买彩票按钮事件
        buyTicketButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
            cl.show(frame.getContentPane(), "buy");
        });

        // 抽奖按钮事件
        drawButton.addActionListener(e -> {
            if (currentUser.getId().equals(ADMIN_ID)) {
                CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
                cl.show(frame.getContentPane(), "draw");
            } else {
                JOptionPane.showMessageDialog(frame, "只有管理员可以进行抽奖！");
            }
        });

        // 查看结果按钮事件
        resultButton.addActionListener(e -> {
            if (!isDrawDone) {
                JOptionPane.showMessageDialog(frame, "尚未进行抽奖！");
                return;
            }
            updateResultPanel();
            CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
            cl.show(frame.getContentPane(), "result");
        });

        // 退出登录按钮事件
        logoutButton.addActionListener(e -> {
            currentUser = null;
            userIdField.setText("");
            loginPasswordField.setText("");
            CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
            cl.show(frame.getContentPane(), "login");
        });
    }

    private void initBuyPanel() {
        buyPanel = new JPanel();
        buyPanel.setLayout(null);

        JLabel titleLabel = new JLabel("购买彩票");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 20));
        titleLabel.setBounds(350, 30, 150, 30);
        buyPanel.add(titleLabel);

        JLabel manualLabel = new JLabel("手动选号 (输入7个1-36的数字，用空格分隔):");
        manualLabel.setBounds(100, 100, 300, 25);
        buyPanel.add(manualLabel);

        JTextField manualNumbersField = new JTextField();
        manualNumbersField.setBounds(420, 100, 200, 25);
        buyPanel.add(manualNumbersField);

        manualButton = new JButton("手动选号购买");
        manualButton.setBounds(200, 150, 150, 30);
        buyPanel.add(manualButton);

        JLabel randomLabel = new JLabel("随机选号:");
        randomLabel.setBounds(100, 220, 100, 25);
        buyPanel.add(randomLabel);

        randomButton = new JButton("随机选号购买");
        randomButton.setBounds(200, 220, 150, 30);
        buyPanel.add(randomButton);

        JLabel betLabel = new JLabel("投注数:");
        betLabel.setBounds(100, 280, 100, 25);
        buyPanel.add(betLabel);

        JTextField betField = new JTextField("1");
        betField.setBounds(200, 280, 50, 25);
        buyPanel.add(betField);

        JLabel balanceLabel = new JLabel("当前余额: ");
        balanceLabel.setBounds(100, 350, 200, 25);
        buyPanel.add(balanceLabel);

        JButton backButton = new JButton("返回");
        backButton.setBounds(350, 450, 100, 30);
        buyPanel.add(backButton);

        // 手动选号购买按钮事件
        manualButton.addActionListener(e -> {
            String numbersStr = manualNumbersField.getText().trim();
            String betStr = betField.getText().trim();

            if (numbersStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入选号！");
                return;
            }

            int bet = 1;
            try {
                bet = Integer.parseInt(betStr);
                if (bet <= 0) {
                    JOptionPane.showMessageDialog(frame, "投注数必须大于0！");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "投注数格式错误！");
                return;
            }

            String[] numberArray = numbersStr.split(" ");
            if (numberArray.length != SELECTED_NUMBERS) {
                JOptionPane.showMessageDialog(frame, "必须选择7个数字！");
                return;
            }

            List<Integer> selectedNumbers = new ArrayList<>();
            try {
                for (String num : numberArray) {
                    int number = Integer.parseInt(num);
                    if (number < 1 || number > TOTAL_NUMBERS) {
                        JOptionPane.showMessageDialog(frame, "数字必须在1-36之间！");
                        return;
                    }
                    if (selectedNumbers.contains(number)) {
                        JOptionPane.showMessageDialog(frame, "数字不能重复！");
                        return;
                    }
                    selectedNumbers.add(number);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "数字格式错误！");
                return;
            }

            double totalPrice = bet * TICKET_PRICE;
            if (currentUser.getAmount() < totalPrice) {
                JOptionPane.showMessageDialog(frame, "余额不足！");
                return;
            }

            // 扣除金额
            currentUser.setAmount(currentUser.getAmount() - totalPrice);

            // 保存彩票
            Ticket ticket = new Ticket(selectedNumbers, bet);
            userTickets.get(currentUser.getId()).add(ticket);

            JOptionPane.showMessageDialog(frame, "购买成功！\n号码: " + selectedNumbers + "\n投注数: " + bet + "\n总价: " + totalPrice + "元");

            // 更新余额显示
            balanceLabel.setText("当前余额: " + currentUser.getAmount() + "元");
        });

        // 随机选号购买按钮事件
        randomButton.addActionListener(e -> {
            String betStr = betField.getText().trim();

            int bet = 1;
            try {
                bet = Integer.parseInt(betStr);
                if (bet <= 0) {
                    JOptionPane.showMessageDialog(frame, "投注数必须大于0！");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "投注数格式错误！");
                return;
            }

            double totalPrice = bet * TICKET_PRICE;
            if (currentUser.getAmount() < totalPrice) {
                JOptionPane.showMessageDialog(frame, "余额不足！");
                return;
            }

            // 生成随机号码
            List<Integer> selectedNumbers = generateRandomNumbers();

            // 扣除金额
            currentUser.setAmount(currentUser.getAmount() - totalPrice);

            // 保存彩票
            Ticket ticket = new Ticket(selectedNumbers, bet);
            userTickets.get(currentUser.getId()).add(ticket);

            JOptionPane.showMessageDialog(frame, "购买成功！\n号码: " + selectedNumbers + "\n投注数: " + bet + "\n总价: " + totalPrice + "元");

            // 更新余额显示
            balanceLabel.setText("当前余额: " + currentUser.getAmount() + "元");
        });

        // 返回按钮事件
        backButton.addActionListener(e -> {
            updateMainPanel();
            CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
            cl.show(frame.getContentPane(), "main");
        });
    }
//抽奖功能实现
    private void initDrawPanel() {
        drawPanel = new JPanel();
        drawPanel.setLayout(null);

        JLabel titleLabel = new JLabel("抽奖");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 20));
        titleLabel.setBounds(350, 30, 100, 30);
        drawPanel.add(titleLabel);

        JLabel drawingLabel = new JLabel("开奖号码:");
        drawingLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        drawingLabel.setBounds(100, 100, 100, 30);
        drawPanel.add(drawingLabel);

        JLabel[] numberLabels = new JLabel[SELECTED_NUMBERS];
        for (int i = 0; i < SELECTED_NUMBERS; i++) {
            numberLabels[i] = new JLabel("0");
            numberLabels[i].setFont(new Font("宋体", Font.BOLD, 24));
            numberLabels[i].setBounds(220 + i * 60, 100, 50, 30);
            numberLabels[i].setHorizontalAlignment(JLabel.CENTER);
            numberLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            drawPanel.add(numberLabels[i]);
        }

        drawButton = new JButton("开始抽奖");
        drawButton.setBounds(350, 200, 100, 30);
        drawPanel.add(drawButton);

        JButton stopButton = new JButton("停止");
        stopButton.setBounds(470, 200, 100, 30);
        stopButton.setEnabled(false);
        drawPanel.add(stopButton);

        JButton backButton = new JButton("返回");
        backButton.setBounds(350, 450, 100, 30);
        drawPanel.add(backButton);

        // 抽奖按钮事件
        drawButton.addActionListener(e -> {
            drawButton.setEnabled(false);
            stopButton.setEnabled(true);

            // 创建一个线程来模拟号码滚动
            Thread drawingThread = new Thread(() -> {
                Random random = new Random();
                int drawCount = 0;

                while (!Thread.interrupted() && drawCount < 50) { // 滚动50次
                    for (int i = 0; i < SELECTED_NUMBERS; i++) {
                        int num = random.nextInt(TOTAL_NUMBERS) + 1;
                        numberLabels[i].setText(String.valueOf(num));
                    }

                    try {
                        Thread.sleep(50); // 控制滚动速度
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    drawCount++;
                }
            });

            drawingThread.start();

            // 停止按钮事件
            stopButton.addActionListener(ee -> {
                // 生成真正的中奖号码
                winningNumbers = generateRandomNumbers();
                for (int i = 0; i < SELECTED_NUMBERS; i++) {
                    numberLabels[i].setText(String.valueOf(winningNumbers.get(i)));
                }

                // 计算中奖结果
                calculateWinners();
                isDrawDone = true;

                drawButton.setEnabled(true);
                stopButton.setEnabled(false);

                JOptionPane.showMessageDialog(frame, "抽奖完成！");
            });
        });

        // 返回按钮事件
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
            cl.show(frame.getContentPane(), "main");
        });
    }
//结果查询与中奖计算
    private void initResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("抽奖结果");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        resultPanel.add(titleLabel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("宋体", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("返回");
        backButton.setPreferredSize(new Dimension(100, 30));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        resultPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 返回按钮事件
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
            cl.show(frame.getContentPane(), "main");
        });
    }

    private void updateMainPanel() {
        if (currentUser == null) return;

        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getText().startsWith("欢迎, ")) {
                    label.setText("欢迎, " + currentUser.getUsername());
                } else if (label.getText().startsWith("账户余额: ")) {
                    label.setText("账户余额: " + currentUser.getAmount() + "元");
                }
            }
        }
    }

    private void updateResultPanel() {
        if (!isDrawDone) {
            resultArea.setText("尚未进行抽奖！");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("开奖号码: ").append(winningNumbers).append("\n\n");
        sb.append("中奖用户:\n");
        sb.append("----------------------------------------\n");

        // 统计各奖项人数
        Map<Integer, Integer> prizeCount = new HashMap<>();
        for (int i = 4; i <= 7; i++) {
            prizeCount.put(i, 0);
        }

        // 查找中奖用户
        boolean hasWinner = false;
        for (Map.Entry<String, List<Ticket>> entry : userTickets.entrySet()) {
            String userId = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            for (Ticket ticket : tickets) {
                int matchCount = countMatchedNumbers(ticket.getNumbers(), winningNumbers);
                if (matchCount >= 4) { // 至少中4个号才算中奖
                    hasWinner = true;
                    User user = users.get(userId);
                    sb.append("用户ID: ").append(userId).append("\n");
                    sb.append("用户名: ").append(user.getUsername()).append("\n");
                    sb.append("投注号码: ").append(ticket.getNumbers()).append("\n");
                    sb.append("匹配数量: ").append(matchCount).append("\n");
                    sb.append("投注数: ").append(ticket.getBet()).append("\n");

                    String prizeName = "";
                    double prizeMoney = 0;
                    switch (matchCount) {
                        case 7:
                            prizeName = "特等奖";
                            prizeMoney = ticket.getBet() * 1000000;
                            break;
                        case 6:
                            prizeName = "一等奖";
                            prizeMoney = ticket.getBet() * 10000;
                            break;
                        case 5:
                            prizeName = "二等奖";
                            prizeMoney = ticket.getBet() * 1000;
                            break;
                        case 4:
                            prizeName = "三等奖";
                            prizeMoney = ticket.getBet() * 100;
                            break;
                    }

                    sb.append("奖项: ").append(prizeName).append("\n");
                    sb.append("奖金: ").append(prizeMoney).append("元\n");
                    sb.append("----------------------------------------\n");

                    // 更新奖项统计
                    prizeCount.put(matchCount, prizeCount.get(matchCount) + 1);
                }
            }
        }

        // 添加奖项统计
        sb.append("\n奖项统计:\n");
        sb.append("特等奖: ").append(prizeCount.get(7)).append("人\n");
        sb.append("一等奖: ").append(prizeCount.get(6)).append("人\n");
        sb.append("二等奖: ").append(prizeCount.get(5)).append("人\n");
        sb.append("三等奖: ").append(prizeCount.get(4)).append("人\n");

        if (!hasWinner) {
            sb.append("暂无中奖用户\n");
        }

        resultArea.setText(sb.toString());
    }
//中奖匹配与奖金计算
    private void checkWinningStatus() {
        if (!isDrawDone || currentUser == null) return;

        List<Ticket> tickets = userTickets.get(currentUser.getId());
        if (tickets == null || tickets.isEmpty()) return;

        boolean hasWinning = false;
        StringBuilder winningMsg = new StringBuilder("恭喜您中奖了！\n\n");

        for (Ticket ticket : tickets) {
            int matchCount = countMatchedNumbers(ticket.getNumbers(), winningNumbers);
            if (matchCount >= 4) {
                hasWinning = true;
                winningMsg.append("投注号码: ").append(ticket.getNumbers()).append("\n");
                winningMsg.append("匹配数量: ").append(matchCount).append("\n");

                String prizeName = "";
                double prizeMoney = 0;
                switch (matchCount) {
                    case 7:
                        prizeName = "特等奖";
                        prizeMoney = ticket.getBet() * 1000000;
                        break;
                    case 6:
                        prizeName = "一等奖";
                        prizeMoney = ticket.getBet() * 10000;
                        break;
                    case 5:
                        prizeName = "二等奖";
                        prizeMoney = ticket.getBet() * 1000;
                        break;
                    case 4:
                        prizeName = "三等奖";
                        prizeMoney = ticket.getBet() * 100;
                        break;
                }

                winningMsg.append("奖项: ").append(prizeName).append("\n");
                winningMsg.append("奖金: ").append(prizeMoney).append("元\n");
                winningMsg.append("------------------------\n");
            }
        }

        if (hasWinning) {
            JOptionPane.showMessageDialog(frame, winningMsg.toString());
        }
    }

    private List<Integer> generateRandomNumbers() {
        List<Integer> numbers = new ArrayList<>();
        Random random = new Random();

        while (numbers.size() < SELECTED_NUMBERS) {
            int num = random.nextInt(TOTAL_NUMBERS) + 1;
            if (!numbers.contains(num)) {
                numbers.add(num);
            }
        }

        // 排序
        Collections.sort(numbers);
        return numbers;
    }

    private void calculateWinners() {
        // 查找中奖用户
        for (Map.Entry<String, List<Ticket>> entry : userTickets.entrySet()) {
            String userId = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            for (Ticket ticket : tickets) {
                int matchCount = countMatchedNumbers(ticket.getNumbers(), winningNumbers);
                if (matchCount >= 4) { // 至少中4个号才算中奖
                    User user = users.get(userId);
                    if (matchCount == 7) {
                        // 特等奖
                        user.setAmount(user.getAmount() + ticket.getBet() * 1000000);
                    } else if (matchCount == 6) {
                        // 一等奖
                        user.setAmount(user.getAmount() + ticket.getBet() * 10000);
                    } else if (matchCount == 5) {
                        // 二等奖
                        user.setAmount(user.getAmount() + ticket.getBet() * 1000);
                    } else if (matchCount == 4) {
                        // 三等奖
                        user.setAmount(user.getAmount() + ticket.getBet() * 100);
                    }
                }
            }
        }
    }

    private int countMatchedNumbers(List<Integer> userNumbers, List<Integer> winningNumbers) {
        int count = 0;
        for (int num : userNumbers) {
            if (winningNumbers.contains(num)) {
                count++;
            }
        }
        return count;
    }

    // 批量注册用户并购买彩票的测试方法
    public void testLotterySystem() {
        // 清空现有用户数据
        users.clear();
        userTickets.clear();

        // 添加管理员用户
        users.put(ADMIN_ID, new User(ADMIN_ID, "管理员", ADMIN_PASSWORD, 10000.0, "0"));
        userTickets.put(ADMIN_ID, new ArrayList<>());

        Random random = new Random();

        // 注册10万个用户
        System.out.println("开始注册用户...");
        for (int i = 1; i <= 100000; i++) {
            String userId = "user" + i;
            String username = "用户" + i;
            String password = "pass" + i;
            String phone = "138" + String.format("%08d", random.nextInt(100000000));
            double amount = random.nextInt(1000) + 100; // 100-1100元之间

            User user = new User(userId, username, password, amount, phone);
            users.put(userId, user);
            userTickets.put(userId, new ArrayList<>());

            // 为每个用户购买彩票
            int ticketCount = random.nextInt(5) + 1; // 1-5张彩票
            for (int j = 0; j < ticketCount; j++) {
                List<Integer> numbers = generateRandomNumbers();
                int bet = random.nextInt(5) + 1; // 1-5注

                double totalPrice = bet * TICKET_PRICE;
                if (user.getAmount() >= totalPrice) {
                    user.setAmount(user.getAmount() - totalPrice);
                    Ticket ticket = new Ticket(numbers, bet);
                    userTickets.get(userId).add(ticket);
                }
            }

            if (i % 10000 == 0) {
                System.out.println("已注册 " + i + " 个用户");
            }
        }

        System.out.println("用户注册完成，开始抽奖...");

        // 模拟抽奖
        winningNumbers = generateRandomNumbers();
        System.out.println("中奖号码: " + winningNumbers);

        // 计算中奖结果
        calculateWinners();
        isDrawDone = true;

        // 输出中奖用户统计
        Map<Integer, Integer> prizeCount = new HashMap<>();
        for (int i = 4; i <= 7; i++) {
            prizeCount.put(i, 0);
        }

        int totalWinners = 0;
        for (Map.Entry<String, List<Ticket>> entry : userTickets.entrySet()) {
            String userId = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            for (Ticket ticket : tickets) {
                int matchCount = countMatchedNumbers(ticket.getNumbers(), winningNumbers);
                if (matchCount >= 4) {
                    totalWinners++;
                    prizeCount.put(matchCount, prizeCount.get(matchCount) + 1);
                }
            }
        }

        System.out.println("抽奖完成！");
        System.out.println("总中奖人数: " + totalWinners);
        System.out.println("特等奖人数: " + prizeCount.get(7));
        System.out.println("一等奖人数: " + prizeCount.get(6));
        System.out.println("二等奖人数: " + prizeCount.get(5));
        System.out.println("三等奖人数: " + prizeCount.get(4));
    }
}
