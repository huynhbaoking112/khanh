package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.bootstrap.ApplicationBootstrap;
import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.dto.ReturnReceipt;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.enums.UserRole;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public final class LibraryShellFrame extends JFrame {
    public LibraryShellFrame(ApplicationBootstrap bootstrap, AuthenticatedSession session) {
        super("Quan Ly Thu Vien");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        add(createHeader(bootstrap, session), BorderLayout.NORTH);
        add(createTabs(bootstrap, session), BorderLayout.CENTER);
    }

    private JPanel createHeader(ApplicationBootstrap bootstrap, AuthenticatedSession session) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        panel.add(new JLabel("Library Management Desktop", SwingConstants.LEFT), BorderLayout.WEST);

        JTextArea info = new JTextArea("""
                Dang nhap: %s (%s)
                Co so du lieu: %s
                """.formatted(session.fullName(), session.role().name(), bootstrap.databasePath().toAbsolutePath()));
        info.setEditable(false);
        info.setOpaque(false);
        panel.add(info, BorderLayout.EAST);
        return panel;
    }

    private JTabbedPane createTabs(ApplicationBootstrap bootstrap, AuthenticatedSession session) {
        JTabbedPane tabs = new JTabbedPane();
        boolean admin = session.role() == UserRole.ADMIN;

        tabs.addTab("Sach", new BookPanel(bootstrap.bookCatalogService(), admin));
        tabs.addTab("Doc Gia", new ReaderPanel(bootstrap.readerManagementService(), admin));
        tabs.addTab("Luu Thong", new CirculationPanel(
                bootstrap.circulationService(),
                bootstrap.historyService(),
                session));
        tabs.addTab("Lich Su", new HistoryPanel(bootstrap.historyService()));

        return tabs;
    }

    private static GridBagConstraints createConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private static void addField(JPanel panel, GridBagConstraints gbc, int row, String label, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private static int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(fieldName + " khong hop le.");
        }
    }

    private static LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new ApplicationException(fieldName + " phai theo dinh dang yyyy-MM-dd.");
        }
    }

    private static List<String> parseCsv(String input) {
        List<String> values = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return values;
        }
        for (String token : input.split(",")) {
            String value = token.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private static void runSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (ApplicationException exception) {
            showError(exception.getMessage());
        } catch (RuntimeException exception) {
            showError(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
        }
    }

    private static void showInfo(String message) {
        JOptionPane.showMessageDialog(null, message, "Thong bao", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }

    private static final class BookPanel extends JPanel {
        private final BookCatalogService service;
        private final boolean canManage;
        private final JTextField searchField = new JTextField(20);
        private final JCheckBox availableOnlyCheck = new JCheckBox("Chi sach con kha dung");
        private final JTextField idField = new JTextField(12);
        private final JTextField titleField = new JTextField(20);
        private final JTextField authorField = new JTextField(20);
        private final JTextField publisherField = new JTextField(20);
        private final JTextField yearField = new JTextField(10);
        private final JTextField totalField = new JTextField(10);
        private final JTextField availableField = new JTextField(10);
        private final JComboBox<BookStatus> statusBox = new JComboBox<>(BookStatus.values());
        private final DefaultTableModel model = new DefaultTableModel(
                new Object[] {"Ma", "Ten Sach", "Tac Gia", "NXB", "Nam", "Tong", "Kha Dung", "Trang Thai"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable table = new JTable(model);

        private BookPanel(BookCatalogService service, boolean canManage) {
            super(new BorderLayout(12, 12));
            this.service = service;
            this.canManage = canManage;
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            buildUi();
            refreshTable();
        }

        private void buildUi() {
            JPanel topBar = new JPanel();
            topBar.add(new JLabel("Tim"));
            topBar.add(searchField);
            topBar.add(availableOnlyCheck);

            JButton searchButton = new JButton("Tra cuu");
            searchButton.addActionListener(event -> refreshTable());
            JButton refreshButton = new JButton("Tai lai");
            refreshButton.addActionListener(event -> {
                searchField.setText("");
                availableOnlyCheck.setSelected(false);
                refreshTable();
            });
            topBar.add(searchButton);
            topBar.add(refreshButton);
            add(topBar, BorderLayout.NORTH);

            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    loadSelectedBook();
                }
            });

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setResizeWeight(0.65);
            splitPane.setLeftComponent(new JScrollPane(table));
            splitPane.setRightComponent(createFormPanel());
            add(splitPane, BorderLayout.CENTER);
        }

        private JPanel createFormPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder(canManage ? "Quan ly sach" : "Thong tin sach"));
            GridBagConstraints gbc = createConstraints();

            addField(panel, gbc, 0, "Ma sach", idField);
            addField(panel, gbc, 1, "Ten sach", titleField);
            addField(panel, gbc, 2, "Tac gia", authorField);
            addField(panel, gbc, 3, "Nha xuat ban", publisherField);
            addField(panel, gbc, 4, "Nam xuat ban", yearField);
            addField(panel, gbc, 5, "So luong tong", totalField);
            addField(panel, gbc, 6, "So luong kha dung", availableField);
            addField(panel, gbc, 7, "Trang thai", statusBox);

            JButton createButton = new JButton("Them moi");
            createButton.setEnabled(canManage);
            createButton.addActionListener(event -> createBook());

            JButton updateButton = new JButton("Cap nhat");
            updateButton.setEnabled(canManage);
            updateButton.addActionListener(event -> updateBook());

            JButton clearButton = new JButton("Xoa form");
            clearButton.addActionListener(event -> clearForm());

            gbc.gridx = 0;
            gbc.gridy = 8;
            gbc.gridwidth = 2;
            JPanel buttons = new JPanel();
            buttons.add(createButton);
            buttons.add(updateButton);
            buttons.add(clearButton);
            panel.add(buttons, gbc);
            return panel;
        }

        private void refreshTable() {
            runSafely(() -> {
                model.setRowCount(0);
                List<Book> books = searchField.getText().isBlank() && !availableOnlyCheck.isSelected()
                        ? service.listBooks()
                        : service.searchBooks(searchField.getText().trim(), availableOnlyCheck.isSelected());
                for (Book book : books) {
                    model.addRow(new Object[] {
                        book.bookId(),
                        book.title(),
                        book.author(),
                        book.publisher(),
                        book.yearPublish(),
                        book.quantityTotal(),
                        book.quantityAvailable(),
                        book.status().name()
                    });
                }
            });
        }

        private void loadSelectedBook() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }
            idField.setText(String.valueOf(model.getValueAt(selectedRow, 0)));
            titleField.setText(String.valueOf(model.getValueAt(selectedRow, 1)));
            authorField.setText(String.valueOf(model.getValueAt(selectedRow, 2)));
            publisherField.setText(String.valueOf(model.getValueAt(selectedRow, 3)));
            yearField.setText(String.valueOf(model.getValueAt(selectedRow, 4)));
            totalField.setText(String.valueOf(model.getValueAt(selectedRow, 5)));
            availableField.setText(String.valueOf(model.getValueAt(selectedRow, 6)));
            statusBox.setSelectedItem(BookStatus.valueOf(String.valueOf(model.getValueAt(selectedRow, 7))));
        }

        private void createBook() {
            runSafely(() -> {
                service.createBook(readBookFromForm());
                refreshTable();
                clearForm();
                showInfo("Da tao sach moi.");
            });
        }

        private void updateBook() {
            runSafely(() -> {
                service.updateBook(readBookFromForm());
                refreshTable();
                showInfo("Da cap nhat sach.");
            });
        }

        private Book readBookFromForm() {
            return new Book(
                    idField.getText().trim(),
                    titleField.getText().trim(),
                    authorField.getText().trim(),
                    publisherField.getText().trim(),
                    parseInt(yearField.getText(), "Nam xuat ban"),
                    parseInt(totalField.getText(), "So luong tong"),
                    parseInt(availableField.getText(), "So luong kha dung"),
                    (BookStatus) statusBox.getSelectedItem());
        }

        private void clearForm() {
            idField.setText("");
            titleField.setText("");
            authorField.setText("");
            publisherField.setText("");
            yearField.setText("");
            totalField.setText("");
            availableField.setText("");
            statusBox.setSelectedItem(BookStatus.AVAILABLE);
        }
    }

    private static final class ReaderPanel extends JPanel {
        private final ReaderManagementService service;
        private final boolean canManage;
        private final JTextField idField = new JTextField(12);
        private final JTextField nameField = new JTextField(20);
        private final JTextField phoneField = new JTextField(16);
        private final JTextField emailField = new JTextField(20);
        private final JTextField maxBorrowField = new JTextField(8);
        private final JComboBox<ReaderStatus> statusBox = new JComboBox<>(ReaderStatus.values());
        private final JTextField currentBorrowField = new JTextField(8);
        private final DefaultTableModel model = new DefaultTableModel(
                new Object[] {"Ma", "Ho ten", "Phone", "Email", "Max", "Trang thai", "Dang muon"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable table = new JTable(model);

        private ReaderPanel(ReaderManagementService service, boolean canManage) {
            super(new BorderLayout(12, 12));
            this.service = service;
            this.canManage = canManage;
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            buildUi();
            refreshTable();
        }

        private void buildUi() {
            JPanel topBar = new JPanel();
            JButton refreshButton = new JButton("Tai lai danh sach");
            refreshButton.addActionListener(event -> refreshTable());
            topBar.add(refreshButton);
            add(topBar, BorderLayout.NORTH);

            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    loadSelectedReader();
                }
            });

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setResizeWeight(0.65);
            splitPane.setLeftComponent(new JScrollPane(table));
            splitPane.setRightComponent(createFormPanel());
            add(splitPane, BorderLayout.CENTER);
        }

        private JPanel createFormPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder(canManage ? "Quan ly doc gia" : "Thong tin doc gia"));
            GridBagConstraints gbc = createConstraints();

            addField(panel, gbc, 0, "Ma doc gia", idField);
            addField(panel, gbc, 1, "Ho ten", nameField);
            addField(panel, gbc, 2, "So dien thoai", phoneField);
            addField(panel, gbc, 3, "Email", emailField);
            addField(panel, gbc, 4, "Gioi han muon", maxBorrowField);
            addField(panel, gbc, 5, "Trang thai", statusBox);
            currentBorrowField.setEditable(false);
            addField(panel, gbc, 6, "Dang muon", currentBorrowField);

            JButton createButton = new JButton("Them moi");
            createButton.setEnabled(canManage);
            createButton.addActionListener(event -> createReader());

            JButton updateButton = new JButton("Cap nhat");
            updateButton.setEnabled(canManage);
            updateButton.addActionListener(event -> updateReader());

            JButton clearButton = new JButton("Xoa form");
            clearButton.addActionListener(event -> clearForm());

            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 2;
            JPanel buttons = new JPanel();
            buttons.add(createButton);
            buttons.add(updateButton);
            buttons.add(clearButton);
            panel.add(buttons, gbc);
            return panel;
        }

        private void refreshTable() {
            runSafely(() -> {
                model.setRowCount(0);
                for (Reader reader : service.listReaders()) {
                    model.addRow(new Object[] {
                        reader.readerId(),
                        reader.fullName(),
                        reader.phone(),
                        reader.email(),
                        reader.maxBorrow(),
                        reader.status().name(),
                        service.getCurrentBorrowCount(reader.readerId())
                    });
                }
            });
        }

        private void loadSelectedReader() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }
            idField.setText(String.valueOf(model.getValueAt(selectedRow, 0)));
            nameField.setText(String.valueOf(model.getValueAt(selectedRow, 1)));
            phoneField.setText(String.valueOf(model.getValueAt(selectedRow, 2)));
            emailField.setText(String.valueOf(model.getValueAt(selectedRow, 3)));
            maxBorrowField.setText(String.valueOf(model.getValueAt(selectedRow, 4)));
            statusBox.setSelectedItem(ReaderStatus.valueOf(String.valueOf(model.getValueAt(selectedRow, 5))));
            currentBorrowField.setText(String.valueOf(model.getValueAt(selectedRow, 6)));
        }

        private void createReader() {
            runSafely(() -> {
                service.createReader(readReaderFromForm());
                refreshTable();
                clearForm();
                showInfo("Da tao doc gia moi.");
            });
        }

        private void updateReader() {
            runSafely(() -> {
                service.updateReader(readReaderFromForm());
                refreshTable();
                showInfo("Da cap nhat doc gia.");
            });
        }

        private Reader readReaderFromForm() {
            return new Reader(
                    idField.getText().trim(),
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim(),
                    parseInt(maxBorrowField.getText(), "Gioi han muon"),
                    (ReaderStatus) statusBox.getSelectedItem());
        }

        private void clearForm() {
            idField.setText("");
            nameField.setText("");
            phoneField.setText("");
            emailField.setText("");
            maxBorrowField.setText("");
            currentBorrowField.setText("");
            statusBox.setSelectedItem(ReaderStatus.ACTIVE);
        }
    }

    private static final class CirculationPanel extends JPanel {
        private final CirculationService circulationService;
        private final HistoryService historyService;
        private final AuthenticatedSession session;
        private final JTextField loanReaderField = new JTextField(12);
        private final JTextField loanBooksField = new JTextField(24);
        private final JTextArea receiptArea = new JTextArea();
        private final JTextField returnLoanIdField = new JTextField(12);
        private final JTextField returnBooksField = new JTextField(24);
        private final JTextField returnDateField = new JTextField(LocalDate.now().toString(), 12);
        private final JTextField renewLoanIdField = new JTextField(12);
        private final JTextField renewDaysField = new JTextField("7", 8);
        private final JTextField renewDateField = new JTextField(LocalDate.now().toString(), 12);

        private CirculationPanel(
                CirculationService circulationService,
                HistoryService historyService,
                AuthenticatedSession session) {
            super(new BorderLayout(12, 12));
            this.circulationService = circulationService;
            this.historyService = historyService;
            this.session = session;
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            buildUi();
        }

        private void buildUi() {
            JPanel forms = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = createConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            forms.add(createLoanPanel(), gbc);

            gbc.gridy = 1;
            forms.add(createReturnPanel(), gbc);

            gbc.gridy = 2;
            forms.add(createRenewPanel(), gbc);

            receiptArea.setEditable(false);
            receiptArea.setLineWrap(true);
            receiptArea.setWrapStyleWord(true);
            receiptArea.setBorder(BorderFactory.createTitledBorder("Ket qua nghiep vu"));
            receiptArea.setText("Thu thu dang dang nhap: %s (%s)".formatted(session.fullName(), session.librarianId()));

            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(forms), new JScrollPane(receiptArea));
            split.setResizeWeight(0.72);
            add(split, BorderLayout.CENTER);
        }

        private JPanel createLoanPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Lap phieu muon"));
            GridBagConstraints gbc = createConstraints();
            addField(panel, gbc, 0, "Ma doc gia", loanReaderField);
            addField(panel, gbc, 1, "Danh sach sach", loanBooksField);

            JButton createButton = new JButton("Tao phieu muon");
            createButton.addActionListener(event -> runSafely(() -> {
                LoanReceipt receipt = circulationService.createLoan(
                        loanReaderField.getText().trim(),
                        session.librarianId(),
                        parseCsv(loanBooksField.getText()));
                receiptArea.setText(formatLoanReceipt(receipt));
                loanReaderField.setText("");
                loanBooksField.setText("");
            }));

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            panel.add(createButton, gbc);
            return panel;
        }

        private JPanel createReturnPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Tra sach"));
            GridBagConstraints gbc = createConstraints();
            addField(panel, gbc, 0, "Ma phieu muon", returnLoanIdField);
            addField(panel, gbc, 1, "Sach tra (bo trong = tat ca)", returnBooksField);
            addField(panel, gbc, 2, "Ngay tra", returnDateField);

            JButton returnButton = new JButton("Xu ly tra sach");
            returnButton.addActionListener(event -> runSafely(() -> {
                List<String> books = parseCsv(returnBooksField.getText());
                if (books.isEmpty()) {
                    books = loadUnreturnedBookIds(returnLoanIdField.getText().trim());
                }
                ReturnReceipt receipt = circulationService.returnBooks(
                        returnLoanIdField.getText().trim(),
                        books,
                        parseDate(returnDateField.getText(), "Ngay tra"));
                receiptArea.setText(formatReturnReceipt(receipt));
            }));

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            panel.add(returnButton, gbc);
            return panel;
        }

        private JPanel createRenewPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder("Gia han"));
            GridBagConstraints gbc = createConstraints();
            addField(panel, gbc, 0, "Ma phieu muon", renewLoanIdField);
            addField(panel, gbc, 1, "So ngay gia han", renewDaysField);
            addField(panel, gbc, 2, "Ngay tham chieu", renewDateField);

            JButton renewButton = new JButton("Gia han");
            renewButton.addActionListener(event -> runSafely(() -> {
                Loan loan = circulationService.renewLoan(
                        renewLoanIdField.getText().trim(),
                        parseInt(renewDaysField.getText(), "So ngay gia han"),
                        parseDate(renewDateField.getText(), "Ngay tham chieu"));
                receiptArea.setText("""
                        Gia han thanh cong
                        Phieu muon: %s
                        Han tra moi: %s
                        Trang thai: %s
                        """.formatted(loan.loanId(), loan.dueDate(), loan.status().name()));
            }));

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            panel.add(renewButton, gbc);
            return panel;
        }

        private List<String> loadUnreturnedBookIds(String loanId) {
            Optional<com.thayhoang.quanly.application.service.dto.LoanRecord> record = historyService.getLoanRecord(loanId);
            if (record.isEmpty()) {
                throw new ApplicationException("Khong tim thay phieu muon de lay danh sach sach tra");
            }
            List<String> ids = new ArrayList<>();
            for (LoanDetail detail : record.get().details()) {
                if (!detail.returned()) {
                    ids.add(detail.bookId());
                }
            }
            return ids;
        }

        private String formatLoanReceipt(LoanReceipt receipt) {
            return """
                    Lap phieu muon thanh cong
                    Ma phieu: %s
                    Doc gia: %s
                    Thu thu: %s
                    Han tra: %s
                    So dau sach: %s
                    """.formatted(
                    receipt.loan().loanId(),
                    receipt.loan().readerId(),
                    receipt.loan().librarianId(),
                    receipt.loan().dueDate(),
                    receipt.details().size());
        }

        private String formatReturnReceipt(ReturnReceipt receipt) {
            String fineText = receipt.fine()
                    .map(fine -> "Tien phat: %s (%s)".formatted(fine.amount(), fine.paidStatus().name()))
                    .orElse("Tien phat: 0");
            return """
                    Tra sach thanh cong
                    Ma phieu: %s
                    Trang thai: %s
                    Ngay tra: %s
                    %s
                    """.formatted(
                    receipt.loan().loanId(),
                    receipt.loan().status().name(),
                    receipt.loan().returnDate(),
                    fineText);
        }
    }

    private static final class HistoryPanel extends JPanel {
        private final HistoryService service;
        private final DefaultTableModel model = new DefaultTableModel(
                new Object[] {"Loan", "Reader", "Librarian", "Loan Date", "Due", "Return", "Status", "Fine"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable table = new JTable(model);
        private final JTextField readerIdField = new JTextField(12);
        private final JTextField loanIdField = new JTextField(12);
        private final JTextArea detailArea = new JTextArea();

        private HistoryPanel(HistoryService service) {
            super(new BorderLayout(12, 12));
            this.service = service;
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            buildUi();
        }

        private void buildUi() {
            JPanel topBar = new JPanel();
            topBar.add(new JLabel("Theo doc gia"));
            topBar.add(readerIdField);

            JButton loadReaderButton = new JButton("Tai lich su");
            loadReaderButton.addActionListener(event -> loadReaderHistory());
            topBar.add(loadReaderButton);

            topBar.add(new JLabel("Theo loan"));
            topBar.add(loanIdField);

            JButton loadLoanButton = new JButton("Xem chi tiet");
            loadLoanButton.addActionListener(event -> loadLoanRecord());
            topBar.add(loadLoanButton);
            add(topBar, BorderLayout.NORTH);

            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                    loanIdField.setText(String.valueOf(model.getValueAt(table.getSelectedRow(), 0)));
                    loadLoanRecord();
                }
            });

            detailArea.setEditable(false);
            detailArea.setLineWrap(true);
            detailArea.setWrapStyleWord(true);

            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JScrollPane(detailArea));
            split.setResizeWeight(0.6);
            add(split, BorderLayout.CENTER);
        }

        private void loadReaderHistory() {
            runSafely(() -> {
                model.setRowCount(0);
                for (com.thayhoang.quanly.application.service.dto.LoanRecord record : service.getReaderHistory(readerIdField.getText().trim())) {
                    model.addRow(new Object[] {
                        record.loan().loanId(),
                        record.reader().fullName(),
                        record.librarian().fullName(),
                        record.loan().loanDate(),
                        record.loan().dueDate(),
                        record.loan().returnDate(),
                        record.loan().status().name(),
                        record.fine().map(Fine::amount).orElse(java.math.BigDecimal.ZERO)
                    });
                }
                detailArea.setText("Da tai " + model.getRowCount() + " dong lich su.");
            });
        }

        private void loadLoanRecord() {
            runSafely(() -> {
                Optional<com.thayhoang.quanly.application.service.dto.LoanRecord> record =
                        service.getLoanRecord(loanIdField.getText().trim());
                if (record.isEmpty()) {
                    detailArea.setText("Khong tim thay phieu muon.");
                    return;
                }

                var loanRecord = record.get();
                StringBuilder detail = new StringBuilder();
                detail.append("Phieu muon: ").append(loanRecord.loan().loanId()).append('\n');
                detail.append("Doc gia: ").append(loanRecord.reader().fullName()).append(" (")
                        .append(loanRecord.reader().readerId()).append(")\n");
                detail.append("Thu thu: ").append(loanRecord.librarian().fullName()).append('\n');
                detail.append("Ngay muon: ").append(loanRecord.loan().loanDate()).append('\n');
                detail.append("Han tra: ").append(loanRecord.loan().dueDate()).append('\n');
                detail.append("Ngay tra: ").append(loanRecord.loan().returnDate()).append('\n');
                detail.append("Trang thai: ").append(loanRecord.loan().status().name()).append("\n\n");
                detail.append("Chi tiet sach:\n");
                for (LoanDetail item : loanRecord.details()) {
                    detail.append("- ").append(item.bookId())
                            .append(" | qty=").append(item.quantity())
                            .append(" | returned=").append(item.returned())
                            .append('\n');
                }
                detail.append('\n');
                detail.append("Phat: ").append(loanRecord.fine()
                        .map(fine -> fine.amount() + " - " + fine.reason() + " - " + fine.paidStatus().name())
                        .orElse("Khong co"));
                detailArea.setText(detail.toString());
            });
        }
    }
}
