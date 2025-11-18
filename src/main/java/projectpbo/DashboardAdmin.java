package projectpbo;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Dashboard Admin UI. Data is supplied via DashboardService; defaults to in-memory demo data.
 */
public class DashboardAdmin {

	private final DashboardService service;
	private final Navigator navigator;
	private BorderPane root;
	private ScrollPane mainScroll;
	private Button backBtn, forwardBtn, homeBtn, logoutBtn;
	private Destination currentDest = null; // null means on main dashboard
	private final Deque<Destination> backStack = new ArrayDeque<>();
	private final Deque<Destination> forwardStack = new ArrayDeque<>();

	public DashboardAdmin() { this(new InMemoryDashboardService()); }
	public DashboardAdmin(DashboardService service) { this(service, null); }
	public DashboardAdmin(DashboardService service, Navigator navigator) {
		this.service = service;
		this.navigator = navigator;
	}

	public static Parent createRoot() { return new DashboardAdmin().build(); }

	public Parent build() {
		root = new BorderPane();
		root.setStyle("-fx-background-color: #f5f7fb;");

		// Sidebar
		VBox sidebar = buildSidebar();
		root.setLeft(sidebar);

		// Top bar
		HBox top = buildTopBar();
		root.setTop(top);

		// Main content (scrollable)
		mainScroll = new ScrollPane(buildContent());
		mainScroll.setFitToWidth(true);
		mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		mainScroll.setStyle("-fx-background-color: transparent;");
		root.setCenter(mainScroll);

		return root;
	}

	private VBox buildSidebar() {
		VBox box = new VBox(8);
		box.setPadding(new Insets(12));
		box.setPrefWidth(200);
		box.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e8edf3; -fx-border-width: 0 1 0 0;");

		Label brand = boldLabel("Hospital Admin", 16, Color.web("#0f172a"));
		brand.setPadding(new Insets(6, 8, 14, 8));

		box.getChildren().add(brand);
		box.getChildren().addAll(
				menuItem("Dashboard Utama", true),
				menuItem("Manajemen Dokter", false),
				menuItem("Tenaga Medis & Staff", false),
				menuItem("Manajemen Pasien", false),
				menuItem("Rawat Inap", false),
				menuItem("IGD", false),
				menuItem("Manajemen Obat", false)
		);
		return box;
	}

	private HBox menuItem(String text, boolean active) {
		Label l = new Label(text);
		l.setFont(Font.font(13));
		l.setTextFill(active ? Color.web("#0b5ed7") : Color.web("#1f2937"));
		HBox row = new HBox(l);
		row.setAlignment(Pos.CENTER_LEFT);
		row.setPadding(new Insets(10, 12, 10, 12));
		row.setStyle(active
				? "-fx-background-color: #e7f1ff; -fx-background-radius: 8;"
				: "-fx-background-color: transparent; -fx-background-radius: 8;");
		return row;
	}

	private HBox buildTopBar() {
		HBox bar = new HBox();
		bar.setPadding(new Insets(12, 20, 12, 20));
		bar.setSpacing(12);
		bar.setAlignment(Pos.CENTER_LEFT);
		bar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e8edf3; -fx-border-width: 0 0 1 0;");

		backBtn = iconButton("back.png", "←", () -> goBack());
		forwardBtn = iconButton("forward.png", "→", () -> goForward());
		homeBtn = iconButton("home.png", "⌂", this::goHome);
		logoutBtn = iconButton("logout.png", "⎋", this::logout);

		Label dot = new Label("•");
		dot.setTextFill(Color.web("#0b5ed7"));
		dot.setFont(Font.font(20));
		Label title = boldLabel("Nasihuy Hospital", 16, Color.web("#0f172a"));
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		bar.getChildren().addAll(backBtn, forwardBtn, homeBtn, dot, title, spacer, logoutBtn);
		updateNavButtons();
		return bar;
	}

	private Parent buildContent() {
		VBox content = new VBox(16);
		content.setPadding(new Insets(20));

		// Header
		VBox header = new VBox(4);
		Label h1 = boldLabel("Dashboard Utama", 22, Color.web("#0f172a"));
		Label hsub = smallLabel("Ringkasan aktivitas rumah sakit hari ini", Color.web("#6b7280"));
		header.getChildren().addAll(h1, hsub);

		// Stats cards
		GridPane grid = new GridPane();
		grid.setHgap(16);
		grid.setVgap(16);

		addStatCard(grid, 0, 0, "Pasien Rawat Jalan", service.getOutpatientCount(), "+12% dari kemarin", Destination.OUTPATIENTS);
		addStatCard(grid, 1, 0, "Pasien Rawat Inap", service.getInpatientCount(), "3 pasien baru", Destination.INPATIENTS);
		addStatCard(grid, 2, 0, "Ketersediaan Kamar", service.getBedAvailable()+"/"+service.getBedTotal(), "37.5% occupancy", Destination.BEDS);
		addStatCard(grid, 0, 1, "Antrian Hari Ini", service.getQueueToday(), "Rata-rata "+service.getAvgWaitMin()+" menit", Destination.QUEUE);
		addStatCard(grid, 1, 1, "Pendapatan Harian", "Rp "+service.getDailyRevenue(), "+8% dari kemarin", Destination.REVENUE_DAILY);
		addStatCard(grid, 2, 1, "Pendapatan Bulanan", "Rp "+service.getMonthlyRevenue(), "+15% dari bulan lalu", Destination.REVENUE_MONTHLY);

		// Schedule today
		VBox schedule = new VBox(12);
		schedule.getChildren().add(sectionTitle("Jadwal Dokter Hari Ini", "Daftar dokter yang bertugas hari ini"));
		VBox list = new VBox(8);
		list.setFillWidth(true);
		list.setPrefWidth(1000);
		for (DoctorSchedule s : service.getTodaySchedules()) {
			list.getChildren().add(scheduleRow(s));
		}
		schedule.getChildren().add(list);

		content.getChildren().addAll(header, grid, schedule);
		return content;
	}

	private void addStatCard(GridPane grid, int col, int row, String title, Object value, String footnote, Destination dest) {
		VBox card = new VBox(8);
		card.setPadding(new Insets(16));
		card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #e8edf3; -fx-border-radius: 10; -fx-cursor: hand;");
		Label t = smallLabel(title, Color.web("#4b5563"));
		Label v = boldLabel(String.valueOf(value), 20, Color.web("#0b5ed7"));
		Label f = smallLabel(footnote, Color.web("#6b7280"));
		card.getChildren().addAll(t, v, f);
		GridPane.setHgrow(card, Priority.ALWAYS);
		card.setMinWidth(260);
		// hover effect & click navigation
		card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #cdd6e2; -fx-border-radius: 10; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.06), 8, 0.0, 0, 2); -fx-cursor: hand;"));
		card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #e8edf3; -fx-border-radius: 10; -fx-cursor: hand;"));
		card.setOnMouseClicked(e -> navigate(dest));
		grid.add(card, col, row);
	}

	private VBox sectionTitle(String title, String subtitle) {
		VBox box = new VBox(4);
		HBox row = new HBox(8);
		Label icon = new Label("⎈");
		icon.setTextFill(Color.web("#0b5ed7"));
		Label t = boldLabel(title, 18, Color.web("#0f172a"));
		row.getChildren().addAll(icon, t);
		Label sub = smallLabel(subtitle, Color.web("#6b7280"));
		box.getChildren().addAll(row, sub);
		return box;
	}

	private HBox scheduleRow(DoctorSchedule s) {
		HBox row = new HBox();
		row.setAlignment(Pos.CENTER_LEFT);
		row.setPadding(new Insets(14));
		row.setSpacing(12);
		row.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #e8edf3; -fx-border-radius: 10; -fx-cursor: hand;");

		VBox left = new VBox(4);
		Label name = boldLabel("Dr. "+s.name(), 14, Color.web("#111827"));
		Label spec = smallLabel(s.specialty(), Color.web("#6b7280"));
		left.getChildren().addAll(name, spec);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label time = smallLabel(String.format("%s - %s", s.start().toString(), s.end().toString()), Color.web("#374151"));
		Label status = chip(s.present() ? "Hadir" : "Belum Hadir", s.present());

		row.getChildren().addAll(left, spacer, time, status);
		row.setOnMouseClicked(e -> navigate(Destination.DOCTORS_SCHEDULE));
		return row;
	}

	private void navigate(Destination dest) {
		if (navigator != null) { navigator.goTo(dest); return; }
		// command-style destinations manage their own history
		if (dest == Destination.HOME) { goHome(); return; }
		if (dest == Destination.BACK) { goBack(); return; }
		if (dest == Destination.FORWARD) { goForward(); return; }
		if (dest == Destination.LOGOUT) { logout(); return; }

		// push current to back stack (use HOME sentinel instead of null)
		backStack.push(currentDest != null ? currentDest : Destination.HOME);
		forwardStack.clear();
		currentDest = dest;
		updateNavButtons();
		// Default behavior: show a simple placeholder page in center
		switch (dest) {
			case OUTPATIENTS -> root.setCenter(pagePlaceholder("Pasien Rawat Jalan", "Daftar/rekap kunjungan hari ini", "Lihat data"));
			case INPATIENTS -> root.setCenter(pagePlaceholder("Pasien Rawat Inap", "Daftar pasien yang masih dirawat", "Lihat data"));
			case BEDS -> root.setCenter(pagePlaceholder("Ketersediaan Kamar", "Status tempat tidur dan okupansi", "Kelola kamar"));
			case QUEUE -> root.setCenter(pagePlaceholder("Antrian Hari Ini", "Pasien dalam antrian layanan", "Kelola antrian"));
			case REVENUE_DAILY -> root.setCenter(pagePlaceholder("Pendapatan Harian", "Ringkasan transaksi hari ini", "Lihat detail"));
			case REVENUE_MONTHLY -> root.setCenter(pagePlaceholder("Pendapatan Bulanan", "Ringkasan transaksi bulanan", "Lihat detail"));
			case DOCTORS_SCHEDULE -> root.setCenter(pagePlaceholder("Jadwal Dokter", "Jadwal praktik hari ini", "Kelola jadwal"));
			default -> { /* no-op for command destinations already handled */ }
		}
	}

	private Parent pagePlaceholder(String title, String subtitle, String action) {
		VBox box = new VBox(8);
		box.setPadding(new Insets(24));
		Label t = boldLabel(title, 20, Color.web("#0f172a"));
		Label s = smallLabel(subtitle, Color.web("#6b7280"));
		Label a = smallLabel(action, Color.web("#2563eb"));
		box.getChildren().addAll(t, s, a);
		ScrollPane sc = new ScrollPane(box);
		sc.setFitToWidth(true);
		return sc;
	}

	private void goHome() {
		if (navigator != null) { navigator.goTo(Destination.HOME); return; }
		if (currentDest != null) {
			backStack.push(currentDest);
			forwardStack.clear();
		}
		currentDest = null;
		root.setCenter(mainScroll);
		updateNavButtons();
	}

	private void updateNavButtons() {
		boolean onHome = (currentDest == null);
		backBtn.setDisable(backStack.isEmpty());
		forwardBtn.setDisable(forwardStack.isEmpty());
		homeBtn.setDisable(onHome);
	}

	private void goBack() {
		if (navigator != null) { navigator.goTo(Destination.BACK); return; }
		if (backStack.isEmpty()) return;
		Destination prev = backStack.pop();
		forwardStack.push(currentDest != null ? currentDest : Destination.HOME);
		if (prev == Destination.HOME) {
			currentDest = null;
			root.setCenter(mainScroll);
		} else {
			currentDest = prev;
			root.setCenter(pagePlaceholderTitle(prev));
		}
		updateNavButtons();
	}

	private void goForward() {
		if (navigator != null) { navigator.goTo(Destination.FORWARD); return; }
		if (forwardStack.isEmpty()) return;
		backStack.push(currentDest != null ? currentDest : Destination.HOME);
		Destination next = forwardStack.pop();
		if (next == Destination.HOME) {
			currentDest = null;
			root.setCenter(mainScroll);
		} else {
			currentDest = next;
			root.setCenter(pagePlaceholderTitle(next));
		}
		updateNavButtons();
	}

	private Parent pagePlaceholderTitle(Destination d) {
		return switch (d) {
			case OUTPATIENTS -> pagePlaceholder("Pasien Rawat Jalan", "Daftar/rekap kunjungan hari ini", "Lihat data");
			case INPATIENTS -> pagePlaceholder("Pasien Rawat Inap", "Daftar pasien yang masih dirawat", "Lihat data");
			case BEDS -> pagePlaceholder("Ketersediaan Kamar", "Status tempat tidur dan okupansi", "Kelola kamar");
			case QUEUE -> pagePlaceholder("Antrian Hari Ini", "Pasien dalam antrian layanan", "Kelola antrian");
			case REVENUE_DAILY -> pagePlaceholder("Pendapatan Harian", "Ringkasan transaksi hari ini", "Lihat detail");
			case REVENUE_MONTHLY -> pagePlaceholder("Pendapatan Bulanan", "Ringkasan transaksi bulanan", "Lihat detail");
			case DOCTORS_SCHEDULE -> pagePlaceholder("Jadwal Dokter", "Jadwal praktik hari ini", "Kelola jadwal");
			case HOME, BACK, FORWARD, LOGOUT -> mainScroll; // not used directly
		};
	}

	private void logout() {
		if (navigator != null) { navigator.goTo(Destination.LOGOUT); return; }
		// Default: replace root with login view on this Stage
		if (root.getScene() != null && root.getScene().getWindow() instanceof Stage stage) {
			stage.getScene().setRoot(LoginView.createRoot(stage));
		}
	}

	private Button iconButton(String resourceName, String fallback, Runnable action) {
		ImageView iv = null;
		try {
			Image img = null;
			if (getClass().getResource("/assets/" + resourceName) != null) {
				img = new Image(getClass().getResource("/assets/" + resourceName).toExternalForm());
			} else if (getClass().getResource("/icons/" + resourceName) != null) {
				img = new Image(getClass().getResource("/icons/" + resourceName).toExternalForm());
			}
			if (img != null) { iv = new ImageView(img); iv.setFitWidth(18); iv.setFitHeight(18); }
		} catch (Exception ignored) {}
		Button b = new Button();
		if (iv != null) b.setGraphic(iv); else b.setText(fallback);
		b.setStyle("-fx-background-color: transparent; -fx-text-fill: #0b5ed7; -fx-font-weight: 600;");
		b.setOnAction(e -> action.run());
		return b;
	}

	private Label chip(String text, boolean positive) {
		Label l = new Label(text);
		l.setPadding(new Insets(4, 8, 4, 8));
		l.setFont(Font.font(12));
		l.setTextFill(positive ? Color.web("#065f46") : Color.web("#92400e"));
		String bg = positive ? "#d1fae5" : "#fef3c7";
		String bd = positive ? "#10b981" : "#f59e0b";
		l.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 999; -fx-border-color: " + bd + "; -fx-border-radius: 999;");
		return l;
	}

	private Label boldLabel(String text, int size, Color color) {
		Label l = new Label(text);
		l.setFont(Font.font("System", FontWeight.BOLD, size));
		l.setTextFill(color);
		return l;
	}

	private Label smallLabel(String text, Color color) {
		Label l = new Label(text);
		l.setFont(Font.font(12));
		l.setTextFill(color);
		return l;
	}

	// ===== Data contracts (DB can implement this later) =====
	public interface DashboardService {
		int getOutpatientCount();
		int getInpatientCount();
		int getBedAvailable();
		int getBedTotal();
		int getQueueToday();
		int getAvgWaitMin();
		String getDailyRevenue();
		String getMonthlyRevenue();
		List<DoctorSchedule> getTodaySchedules();
	}

	public enum Destination {
		HOME, BACK, FORWARD, LOGOUT,
		OUTPATIENTS, INPATIENTS, BEDS, QUEUE, REVENUE_DAILY, REVENUE_MONTHLY, DOCTORS_SCHEDULE
	}
	public interface Navigator { void goTo(Destination destination); }

	public static class InMemoryDashboardService implements DashboardService {
		@Override public int getOutpatientCount() { return 45; }
		@Override public int getInpatientCount() { return 28; }
		@Override public int getBedAvailable() { return 15; }
		@Override public int getBedTotal() { return 40; }
		@Override public int getQueueToday() { return 12; }
		@Override public int getAvgWaitMin() { return 20; }
		@Override public String getDailyRevenue() { return "45.5jt"; }
		@Override public String getMonthlyRevenue() { return "1.2M"; }
		@Override public List<DoctorSchedule> getTodaySchedules() {
			return List.of(
					new DoctorSchedule("Ahmad Rizki", "Kardiologi", LocalTime.of(8,0), LocalTime.of(12,0), true),
					new DoctorSchedule("Sarah Putri", "Pediatri", LocalTime.of(8,0), LocalTime.of(14,0), true),
					new DoctorSchedule("Budi Santoso", "Bedah", LocalTime.of(10,0), LocalTime.of(16,0), true),
					new DoctorSchedule("Rina Wati", "Mata", LocalTime.of(13,0), LocalTime.of(17,0), false)
			);
		}
	}

	public static record DoctorSchedule(String name, String specialty, LocalTime start, LocalTime end, boolean present) {}
}
