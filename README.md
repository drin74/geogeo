import customtkinter as ctk
from tkinter import filedialog, messagebox
import csv
import json
import os
from datetime import datetime
from collections import defaultdict

# ============ НАСТРОЙКИ ПО УМОЛЧАНИЮ ============
DEFAULT_SETTINGS = {
    "theme": "system",          # system / dark / light
    "accent_color": "blue",     # blue / green / purple / orange / red
    "currency": "₽",
    "font_size": 14
}

DATA_FILE = "buh_data.json"
SETTINGS_FILE = "buh_settings.json"

# Цвета акцентов
ACCENT_COLORS = {
    "blue":   ("#1f6aa5", "#36719f"),
    "green":  ("#1b5d3a", "#2fa572"),
    "purple": ("#5b2d8e", "#8b5fbf"),
    "orange": ("#cc6600", "#e68a2e"),
    "red":    ("#a62c2c", "#d94452"),
}


class BuhCalculator(ctk.CTk):
    def __init__(self):
        super().__init__()

        self.title("📊 Бухгалтерский калькулятор")
        self.geometry("1100x700")
        self.minsize(900, 600)

        # Загружаем настройки и данные
        self.settings = self._load_settings()
        self.operations = self._load_data()

        # Применяем настройки
        self._apply_settings()

        # Списки категорий
        self.income_categories = ["Продажи", "Услуги", "Инвестиции", "Прочие доходы"]
        self.expense_categories = ["Аренда", "Зарплата", "Налоги", "Закупки", "Реклама", "Прочие расходы"]

        self._build_ui()
        self._refresh_journal()

    # ============ ЗАГРУЗКА / СОХРАНЕНИЕ ============
    def _load_settings(self):
        if os.path.exists(SETTINGS_FILE):
            try:
                with open(SETTINGS_FILE, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    merged = DEFAULT_SETTINGS.copy()
                    merged.update(data)
                    return merged
            except Exception:
                pass
        return DEFAULT_SETTINGS.copy()

    def _save_settings(self):
        with open(SETTINGS_FILE, "w", encoding="utf-8") as f:
            json.dump(self.settings, f, ensure_ascii=False, indent=2)

    def _load_data(self):
        if os.path.exists(DATA_FILE):
            try:
                with open(DATA_FILE, "r", encoding="utf-8") as f:
                    return json.load(f)
            except Exception:
                pass
        return []

    def _save_data(self):
        with open(DATA_FILE, "w", encoding="utf-8") as f:
            json.dump(self.operations, f, ensure_ascii=False, indent=2)

    # ============ ПРИМЕНЕНИЕ НАСТРОЕК ============
    def _apply_settings(self):
        ctk.set_appearance_mode(self.settings["theme"])
        colors = ACCENT_COLORS.get(self.settings["accent_color"], ACCENT_COLORS["blue"])
        ctk.set_default_color_theme("blue")  # базовая тема
        # Перестраиваем окно при смене акцента
        self.configure(fg_color=("gray90", "gray13"))

    # ============ ПОСТРОЕНИЕ UI ============
    def _build_ui(self):
        # Боковая панель с навигацией
        self.sidebar = ctk.CTkFrame(self, width=200, corner_radius=0)
        self.sidebar.pack(side="left", fill="y")
        self.sidebar.pack_propagate(False)

        logo = ctk.CTkLabel(self.sidebar, text="💼 БУХ\nКАЛЬКУЛЯТОР",
                            font=ctk.CTkFont(size=18, weight="bold"))
        logo.pack(pady=(20, 30))

        self.nav_buttons = {}
        nav_items = [
            ("🧮", "Калькулятор"),
            ("📋", "Журнал"),
            ("📈", "Отчёты"),
            ("🎨", "Дизайн"),
        ]
        for icon, name in nav_items:
            btn = ctk.CTkButton(self.sidebar, text=f"{icon}  {name}",
                                anchor="w", height=40,
                                command=lambda n=name: self._show_tab(n))
            btn.pack(pady=5, padx=15, fill="x")
            self.nav_buttons[name] = btn

        # Кнопка выхода
        ctk.CTkButton(self.sidebar, text="🚪 Выход", fg_color="gray40",
                      hover_color="gray30", command=self.destroy).pack(
                      side="bottom", pady=20, padx=15, fill="x")

        # Основная область
        self.main = ctk.CTkFrame(self, corner_radius=0, fg_color="transparent")
        self.main.pack(side="right", fill="both", expand=True, padx=15, pady=15)

        # Контейнеры вкладок
        self.tabs = {}
        self._build_calculator_tab()
        self._build_journal_tab()
        self._build_reports_tab()
        self._build_design_tab()

        self._show_tab("Калькулятор")

    def _show_tab(self, name):
        for tab_name, frame in self.tabs.items():
            frame.pack_forget()
        self.tabs[name].pack(fill="both", expand=True)
        if name == "Отчёты":
            self._update_reports()
        if name == "Журнал":
            self._refresh_journal()

    # ============ ВКЛАДКА: КАЛЬКУЛЯТОР ============
    def _build_calculator_tab(self):
        tab = ctk.CTkFrame(self.main)
        self.tabs["Калькулятор"] = tab

        title = ctk.CTkLabel(tab, text="🧮 Новая операция",
                             font=ctk.CTkFont(size=22, weight="bold"))
        title.pack(pady=(10, 20))

        form = ctk.CTkFrame(tab)
        form.pack(padx=20, pady=10, fill="x")

        # Тип операции
        ctk.CTkLabel(form, text="Тип операции:").grid(row=0, column=0, padx=10, pady=10, sticky="w")
        self.op_type = ctk.CTkSegmentedButton(form, values=["Доход", "Расход"])
        self.op_type.set("Доход")
        self.op_type.grid(row=0, column=1, padx=10, pady=10)
        self.op_type.configure(command=self._on_type_change)

        # Сумма
        ctk.CTkLabel(form, text="Сумма:").grid(row=1, column=0, padx=10, pady=10, sticky="w")
        self.amount_entry = ctk.CTkEntry(form, placeholder_text="0.00", width=250)
        self.amount_entry.grid(row=1, column=1, padx=10, pady=10)

        # Категория
        ctk.CTkLabel(form, text="Категория:").grid(row=2, column=0, padx=10, pady=10, sticky="w")
        self.category_combo = ctk.CTkComboBox(form, values=self.income_categories, width=250)
        self.category_combo.grid(row=2, column=1, padx=10, pady=10)

        # Описание
        ctk.CTkLabel(form, text="Описание:").grid(row=3, column=0, padx=10, pady=10, sticky="w")
        self.desc_entry = ctk.CTkEntry(form, placeholder_text="Комментарий", width=250)
        self.desc_entry.grid(row=3, column=1, padx=10, pady=10)

        # НДС
        ctk.CTkLabel(form, text="НДС (20%):").grid(row=4, column=0, padx=10, pady=10, sticky="w")
        self.vat_var = ctk.BooleanVar(value=False)
        self.vat_switch = ctk.CTkSwitch(form, text="Включить НДС", variable=self.vat_var)
        self.vat_switch.grid(row=4, column=1, padx=10, pady=10, sticky="w")

        # Дата
        ctk.CTkLabel(form, text="Дата:").grid(row=5, column=0, padx=10, pady=10, sticky="w")
        self.date_entry = ctk.CTkEntry(form, width=250)
        self.date_entry.insert(0, datetime.now().strftime("%Y-%m-%d"))
        self.date_entry.grid(row=5, column=1, padx=10, pady=10)

        # Кнопка добавить
        ctk.CTkButton(tab, text="✅ Добавить операцию", height=40,
                      font=ctk.CTkFont(size=14, weight="bold"),
                      command=self._add_operation).pack(pady=20)

        # Мини-сводка
        summary_frame = ctk.CTkFrame(tab)
        summary_frame.pack(padx=20, pady=10, fill="x")

        self.lbl_income = ctk.CTkLabel(summary_frame, text="Доходы: 0 ₽",
                                       font=ctk.CTkFont(size=16), text_color="#2fa572")
        self.lbl_income.pack(side="left", expand=True, pady=10)

        self.lbl_expense = ctk.CTkLabel(summary_frame, text="Расходы: 0 ₽",
                                        font=ctk.CTkFont(size=16), text_color="#d94452")
        self.lbl_expense.pack(side="left", expand=True, pady=10)

        self.lbl_balance = ctk.CTkLabel(summary_frame, text="Баланс: 0 ₽",
                                        font=ctk.CTkFont(size=16, weight="bold"))
        self.lbl_balance.pack(side="left", expand=True, pady=10)

        self._update_summary()

    def _on_type_change(self):
        if self.op_type.get() == "Доход":
            self.category_combo.configure(values=self.income_categories)
            self.category_combo.set(self.income_categories[0])
        else:
            self.category_combo.configure(values=self.expense_categories)
            self.category_combo.set(self.expense_categories[0])

    def _add_operation(self):
        try:
            amount = float(self.amount_entry.get().replace(",", "."))
        except ValueError:
            messagebox.showerror("Ошибка", "Введите корректную сумму!")
            return

        if amount <= 0:
            messagebox.showerror("Ошибка", "Сумма должна быть больше 0!")
            return

        op_type = self.op_type.get()
        category = self.category_combo.get()
        description = self.desc_entry.get() or "—"
        date = self.date_entry.get() or datetime.now().strftime("%Y-%m-%d")
        vat = self.vat_var.get()

        # Расчёт НДС
        vat_amount = 0.0
        if vat:
            vat_amount = round(amount * 0.20 / 1.20, 2)  # НДС из суммы

        operation = {
            "id": len(self.operations) + 1,
            "date": date,
            "type": op_type,
            "category": category,
            "amount": amount,
            "vat": vat_amount,
            "description": description
        }
        self.operations.append(operation)
        self._save_data()
        self._update_summary()

        # Очистка формы
        self.amount_entry.delete(0, "end")
        self.desc_entry.delete(0, "end")
        self.vat_var.set(False)
        self.date_entry.delete(0, "end")
        self.date_entry.insert(0, datetime.now().strftime("%Y-%m-%d"))

        messagebox.showinfo("Успех", f"Операция добавлена!\n{op_type}: {amount} {self.settings['currency']}")

    def _update_summary(self):
        income = sum(op["amount"] for op in self.operations if op["type"] == "Доход")
        expense = sum(op["amount"] for op in self.operations if op["type"] == "Расход")
        balance = income - expense
        cur = self.settings["currency"]
        self.lbl_income.configure(text=f"Доходы: {income:,.2f} {cur}".replace(",", " "))
        self.lbl_expense.configure(text=f"Расходы: {expense:,.2f} {cur}".replace(",", " "))
        self.lbl_balance.configure(text=f"Баланс: {balance:,.2f} {cur}".replace(",", " "),
                                   text_color="#2fa572" if balance >= 0 else "#d94452")

    # ============ ВКЛАДКА: ЖУРНАЛ ============
    def _build_journal_tab(self):
        tab = ctk.CTkFrame(self.main)
        self.tabs["Журнал"] = tab

        title = ctk.CTkLabel(tab, text="📋 Журнал операций",
                             font=ctk.CTkFont(size=22, weight="bold"))
        title.pack(pady=(10, 10))

        # Фильтры
        filter_frame = ctk.CTkFrame(tab)
        filter_frame.pack(padx=20, pady=5, fill="x")

        ctk.CTkLabel(filter_frame, text="Тип:").pack(side="left", padx=5)
        self.filter_type = ctk.CTkComboBox(filter_frame, values=["Все", "Доход", "Расход"],
                                           width=120, command=lambda _: self._refresh_journal())
        self.filter_type.set("Все")
        self.filter_type.pack(side="left", padx=5)

        ctk.CTkButton(filter_frame, text="🗑 Удалить выбранное", fg_color="#a62c2c",
                      hover_color="#d94452", command=self._delete_selected).pack(side="right", padx=5)

        # Скроллируемая таблица
        self.journal_scroll = ctk.CTkScrollableFrame(tab)
        self.journal_scroll.pack(padx=20, pady=10, fill="both", expand=True)

        self.journal_rows = []
        self.selected_row = None

    def _refresh_journal(self):
        for widget in self.journal_scroll.winfo_children():
            widget.destroy()
        self.journal_rows = []

        # Заголовки
        headers = ["Дата", "Тип", "Категория", "Сумма", "НДС", "Описание"]
        header_frame = ctk.CTkFrame(self.journal_scroll, fg_color="transparent")
        header_frame.pack(fill="x", pady=(0, 5))
        for i, h in enumerate(headers):
            ctk.CTkLabel(header_frame, text=h, font=ctk.CTkFont(weight="bold"),
                         width=120 if i != 5 else 200).pack(side="left", padx=5)

        # Фильтрация
        filter_t = self.filter_type.get()
        filtered = [op for op in self.operations if filter_t == "Все" or op["type"] == filter_t]

        cur = self.settings["currency"]
        for op in reversed(filtered):
            row = ctk.CTkFrame(self.journal_scroll)
            row.pack(fill="x", pady=2)

            color = "#2fa572" if op["type"] == "Доход" else "#d94452"
            values = [
                op["date"],
                op["type"],
                op["category"],
                f"{op['amount']:,.2f} {cur}".replace(",", " "),
                f"{op['vat']:,.2f} {cur}".replace(",", " ") if op["vat"] else "—",
                op["description"]
            ]
            for i, v in enumerate(values):
                lbl = ctk.CTkLabel(row, text=v, width=120 if i != 5 else 200,
                                   text_color=color if i == 1 else None)
                lbl.pack(side="left", padx=5)

            row.bind("<Button-1>", lambda e, r=row: self._select_row(r))
            for child in row.winfo_children():
                child.bind("<Button-1>", lambda e, r=row: self._select_row(r))

            self.journal_rows.append((row, op))

        if not filtered:
            ctk.CTkLabel(self.journal_scroll, text="Операций пока нет",
                         text_color="gray").pack(pady=20)

    def _select_row(self, row):
        for r, _ in self.journal_rows:
            r.configure(fg_color=("gray85", "gray20"))
        row.configure(fg_color=("gray75", "gray35"))
        self.selected_row = row

    def _delete_selected(self):
        if not self.selected_row:
            messagebox.showinfo("Инфо", "Сначала выберите строку")
            return
        for row, op in self.journal_rows:
            if row == self.selected_row:
                self.operations.remove(op)
                self._save_data()
                self._update_summary()
                self._refresh_journal()
                self.selected_row = None
                break

    # ============ ВКЛАДКА: ОТЧЁТЫ ============
    def _build_reports_tab(self):
        tab = ctk.CTkFrame(self.main)
        self.tabs["Отчёты"] = tab

        title = ctk.CTkLabel(tab, text="📈 Отчёты и аналитика",
                             font=ctk.CTkFont(size=22, weight="bold"))
        title.pack(pady=(10, 10))

        # Кнопки экспорта
        btn_frame = ctk.CTkFrame(tab)
        btn_frame.pack(padx=20, pady=10, fill="x")

        ctk.CTkButton(btn_frame, text="📥 Экспорт в CSV",
                      command=self._export_csv).pack(side="left", padx=10, pady=10)
        ctk.CTkButton(btn_frame, text="📥 Экспорт отчёта (сводка)",
                      command=self._export_summary).pack(side="left", padx=10, pady=10)

        # Сводка
        self.report_frame = ctk.CTkScrollableFrame(tab)
        self.report_frame.pack(padx=20, pady=10, fill="both", expand=True)

    def _update_reports(self):
        for w in self.report_frame.winfo_children():
            w.destroy()

        cur = self.settings["currency"]
        income = sum(op["amount"] for op in self.operations if op["type"] == "Доход")
        expense = sum(op["amount"] for op in self.operations if op["type"] == "Расход")
        vat_total = sum(op["vat"] for op in self.operations)

        ctk.CTkLabel(self.report_frame, text="📊 Общая сводка",
                     font=ctk.CTkFont(size=18, weight="bold")).pack(anchor="w", pady=5)

        summary = [
            ("Всего операций:", f"{len(self.operations)}"),
            ("Общие доходы:", f"{income:,.2f} {cur}".replace(",", " ")),
            ("Общие расходы:", f"{expense:,.2f} {cur}".replace(",", " ")),
            ("Чистая прибыль:", f"{income - expense:,.2f} {cur}".replace(",", " ")),
            ("Начислено НДС:", f"{vat_total:,.2f} {cur}".replace(",", " ")),
        ]
        for label, value in summary:
            row = ctk.CTkFrame(self.report_frame)
            row.pack(fill="x", pady=2)
            ctk.CTkLabel(row, text=label, width=200, anchor="w").pack(side="left", padx=10)
            ctk.CTkLabel(row, text=value, font=ctk.CTkFont(weight="bold")).pack(side="left", padx=10)

        # По категориям
        ctk.CTkLabel(self.report_frame, text="📂 По категориям",
                     font=ctk.CTkFont(size=18, weight="bold")).pack(anchor="w", pady=(15, 5))

        by_cat = defaultdict(float)
        for op in self.operations:
            by_cat[f"{op['type']} / {op['category']}"] += op["amount"]

        for cat, total in sorted(by_cat.items()):
            row = ctk.CTkFrame(self.report_frame)
            row.pack(fill="x", pady=2)
            ctk.CTkLabel(row, text=cat, width=250, anchor="w").pack(side="left", padx=10)
            ctk.CTkLabel(row, text=f"{total:,.2f} {cur}".replace(",", " "),
                         font=ctk.CTkFont(weight="bold")).pack(side="left", padx=10)

    def _export_csv(self):
        if not self.operations:
            messagebox.showinfo("Инфо", "Нет данных для экспорта")
            return
        path = filedialog.asksaveasfilename(defaultextension=".csv",
                                            filetypes=[("CSV файлы", "*.csv")],
                                            initialfile=f"operations_{datetime.now():%Y%m%d}.csv")
        if not path:
            return
        try:
            with open(path, "w", encoding="utf-8-sig", newline="") as f:
                writer = csv.writer(f, delimiter=";")
                writer.writerow(["Дата", "Тип", "Категория", "Сумма", "НДС", "Описание"])
                for op in self.operations:
                    writer.writerow([op["date"], op["type"], op["category"],
                                     op["amount"], op["vat"], op["description"]])
            messagebox.showinfo("Успех", f"Экспорт завершён:\n{path}")
        except Exception as e:
            messagebox.showerror("Ошибка", str(e))

    def _export_summary(self):
        path = filedialog.asksaveasfilename(defaultextension=".csv",
                                            filetypes=[("CSV файлы", "*.csv")],
                                            initialfile=f"summary_{datetime.now():%Y%m%d}.csv")
        if not path:
            return
        try:
            income = sum(op["amount"] for op in self.operations if op["type"] == "Доход")
            expense = sum(op["amount"] for op in self.operations if op["type"] == "Расход")
            vat_total = sum(op["vat"] for op in self.operations)

            by_cat = defaultdict(float)
            for op in self.operations:
                by_cat[f"{op['type']} / {op['category']}"] += op["amount"]

            with open(path, "w", encoding="utf-8-sig", newline="") as f:
                writer = csv.writer(f, delimiter=";")
                writer.writerow(["Отчёт", f"от {datetime.now():%Y-%m-%d %H:%M}"])
                writer.writerow([])
                writer.writerow(["Показатель", "Значение"])
                writer.writerow(["Всего операций", len(self.operations)])
                writer.writerow(["Доходы", f"{income:.2f}"])
                writer.writerow(["Расходы", f"{expense:.2f}"])
                writer.writerow(["Прибыль", f"{income - expense:.2f}"])
                writer.writerow(["НДС", f"{vat_total:.2f}"])
                writer.writerow([])
                writer.writerow(["Категория", "Сумма"])
                for cat, total in sorted(by_cat.items()):
                    writer.writerow([cat, f"{total:.2f}"])
            messagebox.showinfo("Успех", f"Отчёт сохранён:\n{path}")
        except Exception as e:
            messagebox.showerror("Ошибка", str(e))

    # ============ ВКЛАДКА: ДИЗАЙН ============
    def _build_design_tab(self):
        tab = ctk.CTkFrame(self.main)
        self.tabs["Дизайн"] = tab

        title = ctk.CTkLabel(tab, text="🎨 Настройки дизайна",
                             font=ctk.CTkFont(size=22, weight="bold"))
        title.pack(pady=(10, 20))

        settings_frame = ctk.CTkFrame(tab)
        settings_frame.pack(padx=40, pady=10, fill="x")

        # Тема
        ctk.CTkLabel(settings_frame, text="Тема оформления:",
                     font=ctk.CTkFont(size=14)).grid(row=0, column=0, padx=15, pady=15, sticky="w")
        self.theme_menu = ctk.CTkSegmentedButton(settings_frame,
                                                 values=["system", "light", "dark"],
                                                 command=self._change_theme)
        self.theme_menu.set(self.settings["theme"])
        self.theme_menu.grid(row=0, column=1, padx=15, pady=15)

        # Акцентный цвет
        ctk.CTkLabel(settings_frame, text="Акцентный цвет:",
                     font=ctk.CTkFont(size=14)).grid(row=1, column=0, padx=15, pady=15, sticky="w")
        self.color_menu = ctk.CTkSegmentedButton(settings_frame,
                                                 values=list(ACCENT_COLORS.keys()),
                                                 command=self._change_color)
        self.color_menu.set(self.settings["accent_color"])
        self.color_menu.grid(row=1, column=1, padx=15, pady=15)

        # Валюта
        ctk.CTkLabel(settings_frame, text="Валюта:",
                     font=ctk.CTkFont(size=14)).grid(row=2, column=0, padx=15, pady=15, sticky="w")
        self.currency_entry = ctk.CTkEntry(settings_frame, width=100)
        self.currency_entry.insert(0, self.settings["currency"])
        self.currency_entry.grid(row=2, column=1, padx=15, pady=15, sticky="w")

        # Размер шрифта
        ctk.CTkLabel(settings_frame, text="Размер шрифта:",
                     font=ctk.CTkFont(size=14)).grid(row=3, column=0, padx=15, pady=15, sticky="w")
        self.font_slider = ctk.CTkSlider(settings_frame, from_=10, to=22,
                                         number_of_steps=6,
                                         command=self._change_font)
        self.font_slider.set(self.settings["font_size"])
        self.font_slider.grid(row=3, column=1, padx=15, pady=15, sticky="we")

        self.font_label = ctk.CTkLabel(settings_frame,
                                       text=f"{self.settings['font_size']} pt")
        self.font_label.grid(row=3, column=2, padx=5)

        # Кнопка применить
        ctk.CTkButton(tab, text="💾 Применить и сохранить",
                      command=self._save_all_settings).pack(pady=20)

        # Превью
        ctk.CTkLabel(tab, text="Превью элементов",
                     font=ctk.CTkFont(size=16, weight="bold")).pack(pady=(20, 5))
        preview = ctk.CTkFrame(tab)
        preview.pack(padx=40, fill="x")
        ctk.CTkButton(preview, text="Пример кнопки").pack(side="left", padx=10, pady=10)
        ctk.CTkEntry(preview, placeholder_text="Пример поля ввода").pack(side="left", padx=10, pady=10)
        ctk.CTkCheckBox(preview, text="Чекбокс").pack(side="left", padx=10, pady=10)

    def _change_theme(self, value):
        self.settings["theme"] = value
        ctk.set_appearance_mode(value)

    def _change_color(self, value):
        self.settings["accent_color"] = value
        # Перезапуск для применения цвета (customtkinter ограничивает динамическую смену)
        self._apply_settings()
        # Перестраиваем UI
        for widget in self.sidebar.winfo_children():
            widget.destroy()
        for widget in self.main.winfo_children():
            widget.destroy()
        self.tabs = {}
        self._build_ui()
        self._refresh_journal()
        self._show_tab("Дизайн")

    def _change_font(self, value):
        size = int(value)
        self.settings["font_size"] = size
        self.font_label.configure(text=f"{size} pt")
        ctk.FontManager().load_default()

    def _save_all_settings(self):
        self.settings["currency"] = self.currency_entry.get() or "₽"
        self._save_settings()
        self._update_summary()
        messagebox.showinfo("Успех", "Настройки сохранены!")


if __name__ == "__main__":
    app = BuhCalculator()
    app.mainloop()
