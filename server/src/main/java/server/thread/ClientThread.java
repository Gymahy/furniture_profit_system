package server.thread;

import enums.RequestType;
import enums.ResponseStatus;
import models.dto.DateRangeDTO;
import models.dto.DeleteRequestDTO;
import models.dto.ExpenseRequestDTO;
import models.dto.IdStatusDTO;
import models.dto.LoginDTO;
import models.dto.PasswordResetDTO;
import models.dto.ReportRequestDTO;
import models.dto.SaleRequestDTO;
import models.dto.StockCheckDTO;
import models.entities.Category;
import models.entities.Customer;
import models.entities.Discount;
import models.entities.Expense;
import models.entities.ExpenseCategory;
import models.entities.Product;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;
import services.CatalogService;
import services.AuditService;
import services.CustomerService;
import services.DiscountService;
import services.ExpenseService;
import services.SaleService;
import services.UserService;
import utility.RoleChecker;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User currentUser;

    private final UserService userService = new UserService();
    private final AuditService auditService = new AuditService();
    private final CatalogService catalogService = new CatalogService();
    private final CustomerService customerService = new CustomerService();
    private final DiscountService discountService = new DiscountService();
    private final ExpenseService expenseService = new ExpenseService();
    private final SaleService saleService = new SaleService();

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (!socket.isClosed()) {
                Object object = in.readObject();
                if (!(object instanceof Request request)) {
                    send(Response.error("Некорректный запрос"));
                    continue;
                }
                send(process(request));
            }
        } catch (EOFException ignored) {
            // Client closed the connection.
        } catch (Exception e) {
            System.err.println("Client thread error: " + e.getMessage());
        } finally {
            close();
        }
    }

    private Response process(Request request) {
        try {
            RequestType type = request.getRequestType();
            if (type == RequestType.LOGIN) {
                LoginDTO login = payload(request, LoginDTO.class);
                currentUser = userService.login(login.getLogin(), login.getPassword());
                return Response.ok("Вход выполнен", currentUser);
            }

            requireAuthenticated();

            return switch (type) {
                case GET_USERS -> {
                    require(RoleChecker.canManageUsers(currentUser));
                    yield Response.ok("Пользователи загружены", userService.findAll());
                }
                case SAVE_USER -> {
                    require(RoleChecker.canManageUsers(currentUser));
                    yield Response.ok("Пользователь сохранен", userService.save(payload(request, User.class), currentUser));
                }
                case DELETE_USER -> {
                    require(RoleChecker.canManageUsers(currentUser));
                    userService.delete(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Пользователь удален", null);
                }
                case SET_USER_ACTIVE -> {
                    require(RoleChecker.canManageUsers(currentUser));
                    IdStatusDTO dto = payload(request, IdStatusDTO.class);
                    userService.setActive(dto.getId(), dto.isActive(), currentUser);
                    yield Response.ok("Статус пользователя изменен", null);
                }
                case RESET_PASSWORD -> {
                    require(RoleChecker.canManageUsers(currentUser));
                    PasswordResetDTO dto = payload(request, PasswordResetDTO.class);
                    userService.resetPassword(dto.getUserId(), dto.getNewPassword(), currentUser);
                    yield Response.ok("Пароль изменен", null);
                }
                case GET_ROLES -> {
                    require(RoleChecker.canManageUsers(currentUser));
                    yield Response.ok("Роли загружены", userService.findRoles());
                }
                case GET_AUDIT_LOGS -> {
                    require(RoleChecker.canManageUsers(currentUser));
                    yield Response.ok("История логов загружена", auditService.findRecent());
                }
                case GET_CATEGORIES -> Response.ok("Категории загружены", catalogService.findCategories());
                case SAVE_CATEGORY -> {
                    require(RoleChecker.canManageCatalog(currentUser));
                    yield Response.ok("Категория сохранена", catalogService.saveCategory(payload(request, Category.class), currentUser));
                }
                case DELETE_CATEGORY -> {
                    require(RoleChecker.canManageCatalog(currentUser));
                    catalogService.deleteCategory(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Категория удалена", null);
                }
                case GET_PRODUCTS -> Response.ok("Товары загружены", catalogService.findProducts());
                case SAVE_PRODUCT -> {
                    require(RoleChecker.canManageCatalog(currentUser));
                    yield Response.ok("Товар сохранен", catalogService.saveProduct(payload(request, Product.class), currentUser));
                }
                case DELETE_PRODUCT -> {
                    require(RoleChecker.canManageCatalog(currentUser));
                    catalogService.deleteProduct(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Товар удален", null);
                }
                case CHECK_STOCK -> {
                    require(RoleChecker.canWorkWithSales(currentUser));
                    yield Response.ok("Остаток проверен", catalogService.checkStock(payload(request, StockCheckDTO.class)));
                }
                case GET_CUSTOMERS -> {
                    require(RoleChecker.canWorkWithCustomers(currentUser));
                    yield Response.ok("Клиенты загружены", customerService.findAll());
                }
                case SAVE_CUSTOMER -> {
                    require(RoleChecker.canWorkWithCustomers(currentUser));
                    yield Response.ok("Клиент сохранен", customerService.save(payload(request, Customer.class), currentUser));
                }
                case DELETE_CUSTOMER -> {
                    require(RoleChecker.canWorkWithCustomers(currentUser));
                    customerService.delete(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Клиент удален", null);
                }
                case GET_CUSTOMER_HISTORY -> {
                    require(RoleChecker.canWorkWithCustomers(currentUser));
                    yield Response.ok("История загружена", customerService.history(payload(request, DeleteRequestDTO.class).getId()));
                }
                case GET_DISCOUNTS -> {
                    Object data = RoleChecker.canManageDiscounts(currentUser) ? discountService.findAll() : discountService.findActive();
                    yield Response.ok("Скидки загружены", data);
                }
                case SAVE_DISCOUNT -> {
                    require(RoleChecker.canManageDiscounts(currentUser));
                    yield Response.ok("Скидка сохранена", discountService.save(payload(request, Discount.class), currentUser));
                }
                case DELETE_DISCOUNT -> {
                    require(RoleChecker.canManageDiscounts(currentUser));
                    discountService.delete(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Скидка удалена", null);
                }
                case GET_EXPENSE_CATEGORIES -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    yield Response.ok("Категории расходов загружены", expenseService.findCategories());
                }
                case SAVE_EXPENSE_CATEGORY -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    yield Response.ok("Категория расхода сохранена", expenseService.saveCategory(payload(request, ExpenseCategory.class), currentUser));
                }
                case DELETE_EXPENSE_CATEGORY -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    expenseService.deleteCategory(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Категория расхода удалена", null);
                }
                case GET_EXPENSES -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    DateRangeDTO range = payload(request, DateRangeDTO.class);
                    yield Response.ok("Расходы загружены", expenseService.findByPeriod(range.getStartDate(), range.getEndDate()));
                }
                case SAVE_EXPENSE -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    ExpenseRequestDTO dto = payload(request, ExpenseRequestDTO.class);
                    yield Response.ok("Расход сохранен", expenseService.save(dto.getExpense(), currentUser, dto.getStartDate(), dto.getEndDate()));
                }
                case DELETE_EXPENSE -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    expenseService.delete(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Расход удален", null);
                }
                case CREATE_SALE -> {
                    require(RoleChecker.canWorkWithSales(currentUser));
                    yield Response.ok("Продажа оформлена", saleService.createSale(payload(request, SaleRequestDTO.class), currentUser));
                }
                case GET_SALES -> {
                    DateRangeDTO range = payload(request, DateRangeDTO.class);
                    if (!RoleChecker.canWorkWithSales(currentUser) && !RoleChecker.canWorkWithFinance(currentUser)) {
                        require(false);
                    }
                    yield Response.ok("Продажи загружены", saleService.findSales(range.getStartDate(), range.getEndDate()));
                }
                case CHANGE_SALE_STATUS -> {
                    require(RoleChecker.canWorkWithSales(currentUser));
                    IdStatusDTO dto = payload(request, IdStatusDTO.class);
                    saleService.changeStatus(dto.getId(), dto.getSaleStatus(), currentUser);
                    yield Response.ok("Статус продажи изменен", null);
                }
                case DELETE_SALE -> {
                    require(RoleChecker.canWorkWithSales(currentUser));
                    saleService.deleteSale(payload(request, DeleteRequestDTO.class).getId(), currentUser);
                    yield Response.ok("Продажа удалена", null);
                }
                case CALCULATE_PROFIT -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    DateRangeDTO range = payload(request, DateRangeDTO.class);
                    yield Response.ok("Прибыль рассчитана", saleService.calculateProfit(range.getStartDate(), range.getEndDate()));
                }
                case GET_FINANCIAL_REPORT -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    yield Response.ok("Отчет сформирован", saleService.buildReport(payload(request, ReportRequestDTO.class)));
                }
                case GET_COMMISSION_REPORT -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    DateRangeDTO range = payload(request, DateRangeDTO.class);
                    yield Response.ok("Комиссии рассчитаны", saleService.findCommissions(range.getStartDate(), range.getEndDate()));
                }
                case EXPORT_REPORT -> {
                    require(RoleChecker.canWorkWithFinance(currentUser));
                    yield Response.ok("Отчет экспортирован", saleService.exportReport(payload(request, ReportRequestDTO.class), currentUser));
                }
                case LOGIN -> Response.error("Повторный вход не требуется");
            };
        } catch (Exception e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    private <T> T payload(Request request, Class<T> type) {
        Object payload = request.getPayload();
        if (!type.isInstance(payload)) {
            throw new IllegalArgumentException("Ожидался объект " + type.getSimpleName());
        }
        return type.cast(payload);
    }

    private void requireAuthenticated() {
        if (currentUser == null) {
            throw new SecurityException("Выполните вход в систему");
        }
    }

    private void require(boolean allowed) {
        if (!allowed) {
            throw new SecurityException("Недостаточно прав для выполнения операции");
        }
    }

    private void send(Response response) throws IOException {
        out.writeObject(response);
        out.flush();
        out.reset();
    }

    private void close() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException ignored) {
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
