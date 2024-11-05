package com.example.tk6;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

//import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MyKeyboardService extends InputMethodService {
    private MyKeyboardView keyboardView;
    private StringBuilder currentText = new StringBuilder();
    private String currentLanguage = "EN"; // Текущий язык по умолчанию
    private boolean isShifted = false; // Переключатель для Shift (регистр букв)
    private boolean isSymbolMode = false; // Переключатель для режима символов
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable backspaceRunnable;
    private Runnable enterRunnable;
    private boolean isBackspacePressed = false; // Состояние нажатия кнопки Backspace

    @Override
    public View onCreateInputView() {
        keyboardView = new MyKeyboardView(this);
        return keyboardView;
    }

    private class MyKeyboardView extends View {
        private Paint paint;
        private float buttonWidth;
        private float buttonHeight;
        private final float keyboardHeight = 900; // Высота клавиатуры
        private final int rows = 7; // Количество рядов
        private final int cols = 7; // Количество кнопок в первых трех рядах
        private final int greenCols = 10; // Количество кнопок в 4 ряду (0-9)

        public MyKeyboardView(MyKeyboardService context) {
            super(context);
            paint = new Paint();
            paint.setTextSize(70); // Размер текста на кнопках
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            buttonHeight = keyboardHeight / rows; // Высота кнопок
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float top = getHeight() - keyboardHeight; // Верхняя граница для кнопок

            // Отрисовка первых трех рядов (голубые кнопки)
            for (int row = 0; row < 3; row++) {
                buttonWidth = getWidth() / cols; // Ширина кнопок для первых трех рядов
                for (int col = 0; col < cols; col++) {
                    int buttonNumber = row * cols + col; // Номер кнопки
                    String label = getButtonLabel(buttonNumber); // Получение текста для кнопки
                    drawButton(canvas, label, col, row, top, buttonNumber);
                }
            }

            // Отрисовка 4-го ряда с 10 светло-зелеными кнопками (0-9)
            buttonWidth = getWidth() / greenCols; // Ширина кнопок для 4-го ряда
            for (int col2 = 0; col2 < greenCols; col2++) {
                String label = String.valueOf(col2); // Кнопки с 0 до 9
                drawButton(canvas, label, col2, 3, top, 21 + col2); // 4-й ряд
            }

            // Отрисовка 5, 6 и 7 рядов (голубые кнопки)
            for (int row = 4; row < rows; row++) {
                buttonWidth = getWidth() / cols; // Ширина кнопок для последних трех рядов
                for (int col = 0; col < cols; col++) {
                    int buttonNumber = (row - 4) * cols + col + 31; // Номер кнопки
                    String label = getButtonLabel(buttonNumber); // Получение текста для кнопки
                    drawButton(canvas, label, col, row, top, buttonNumber);
                }
            }
        }

        // Получение текста для кнопки в зависимости от текущего режима
        private String getButtonLabel(int buttonNumber) {
            if (buttonNumber == 8) {
                return "↑"; // Двунаправленная стрелка
            }
            if (buttonNumber == 12) {
                return "←"; // Двунаправленная стрелка
            }
            if (buttonNumber == 34) {
                return currentLanguage; // Двунаправленная стрелка
            }
            if (buttonNumber == 39) {
                return " "; // Двунаправленная стрелка
            }
            if (buttonNumber == 41) {
                return "⬌"; // Двунаправленная стрелка
            }
            if (buttonNumber == 43) {
                return "↓"; // Двунаправленная стрелка
            }
            if (buttonNumber == 48) {
                return "="; // Двунаправленная стрелка
            }

            if (isSymbolMode) {
                return getSymbol(buttonNumber); // Символы, если включен режим символов
            } else {
                return getLetter(buttonNumber); // Буквы, если включен обычный режим
            }
        }

        // Получение символов для голубых кнопок
        private String getSymbol(int index) {
            String[] symbols = getKeyboardSymbols();
            if (index >= 0 && index < symbols.length) {
                return symbols[index];
            }
            return "";
        }

        // Символы клавиатуры для режима символов
        private String[] getKeyboardSymbols() {
            return new String[]{
                    "!", "@", "#","", "$", "%", "^", "&","", "*","", "(","", ")", "-", "_", "=","", "+", "[", "]","","","","","","","","","","", "{", "}", ";","", ":", "'", "\"", "<","", ">","", ",","", ".", "/", "?", "\\","", "|", "~", ""
            };
        }

        // Получение букв алфавита в зависимости от текущего языка и регистра
        private String getLetter(int index) {
            String[] alphabet = getCurrentAlphabet();
            if (index >= 0 && index < alphabet.length) {
                String letter = alphabet[index];
                return isShifted ? letter.toUpperCase() : letter.toLowerCase(); // Смена регистра
            }
            return "";
        }

        // Получение текущего алфавита в зависимости от выбранного языка
        private String[] getCurrentAlphabet() {
            switch (currentLanguage) {
                case "RU":
                    return getRussianAlphabet();
                case "UA":
                    return getUkrainianAlphabet();
                case "EN":
                default:
                    return getEnglishAlphabet();
            }
        }

        private String[] getEnglishAlphabet() {
            return new String[]{
                    "a", "b", "c", "", "d", "e", "f", "g", "", "h", "", "i", "", "j", "k", "l", "m", "", "n", "o", "p", "", "", "", "", "","","","","","", "q", "r", "s", "", "t", "u", "v", "w", "", "x", "", "y", "", "z", "", "", "", "", "", "", ""
            };
        }

        private String[] getRussianAlphabet() {
            return new String[]{
                    "а", "б", "в", "ъ", "г", "д", "е", "ё","", "ж","", "з","", "и", "й", "к", "л","", "м", "н", "о","","","","","","","","","","", "п", "р", "с","", "т", "у", "ф", "х","", "ц","", "ч", "","ш", "щ", "ы", "ь","", "э", "ю", "я"
            };
        }

        private String[] getUkrainianAlphabet() {
            return new String[]{
                    "а", "б", "в","", "г", "д", "е", "є","", "ж","", "з","", "и", "і", "ї", "й","", "к", "л", "м","","","","","","","","","","", "н", "о", "п","", "р", "с", "т", "у","", "ф","", "х","", "ц", "ч", "ш", "щ","", "ь", "ю", "я"
            };
        }
        // Метод для отрисовки кнопок с закругленными краями и белыми границами
        private void drawButton(Canvas canvas, String label, int col, int row, float top, int buttonNumber) {
            float left = col * buttonWidth;
            float right = left + buttonWidth;
            float bottom = top + (row + 1) * buttonHeight;
            float cornerRadius = 20; // Радиус закругления углов


            // Задаем цвет кнопки
            if (buttonNumber == 3 || buttonNumber == 8 || buttonNumber == 10 || buttonNumber == 12 || buttonNumber == 17 || buttonNumber == 34 || buttonNumber == 39 || buttonNumber == 41 || buttonNumber == 43|| buttonNumber == 48) {
                paint.setColor(Color.rgb(255, 182, 193)); // Светло-розовый для специальных кнопок
            } else if (buttonNumber >= 21 && buttonNumber < 31) {
                paint.setColor(Color.rgb(144, 238, 144)); // Светло-зеленый цвет для новых кнопок в 4-м ряду (0-9)
            } else {
                paint.setColor(Color.rgb(173, 216, 230)); // Голубой для обычных кнопок
            }

            // canvas.drawRect(left, top + row * buttonHeight, right, bottom, paint);
            // Рисуем закругленную кнопку
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(left, top + row * buttonHeight, right, bottom, cornerRadius, cornerRadius, paint);

            // Рисуем белую границу вокруг кнопки
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4); // Толщина границы
            canvas.drawRoundRect(left, top + row * buttonHeight, right, bottom, cornerRadius, cornerRadius, paint);

            // Рисуем текст кнопки
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL); // Восстанавливаем стиль для текста
            float textWidth = paint.measureText(label);
            float textX = left + (buttonWidth - textWidth) / 2;
            float textY = bottom - (buttonHeight / 2) + (paint.getTextSize() / 2.5f);
            canvas.drawText(label, textX, textY, paint);
            // Рисуем текст кнопки

        }
        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            float y = event.getY();
            float keyboardTop = getHeight() - keyboardHeight; // Верхняя граница клавиатуры

            // Если касание произошло выше клавиатуры, позволяем системе обработать касание
            if (y < keyboardTop) {
                return super.dispatchTouchEvent(event);
            }

            // Если касание произошло в пределах клавиатуры, обрабатываем его в handleKeyboardTouchEvent
            if (handleKeyboardTouchEvent(event)) {
                return true;
            }

            // Если касание не было обработано, передаем его системе
            return super.dispatchTouchEvent(event);
        }

        private boolean handleKeyboardTouchEvent(MotionEvent event) {
            // Логика обработки касаний для клавиатуры
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();

                // Определяем, какая кнопка была нажата, если касание произошло в области клавиатуры
                if (y >= getHeight() - keyboardHeight) {
                    int row = (int) ((y - (getHeight() - keyboardHeight)) / buttonHeight);
                    int col = (int) (x / buttonWidth);
                    int col1 = (int) (x / buttonWidth * 1.43);

                    if (row < 3 && col < cols) {
                        int buttonNumber = row * cols + col; // Номер кнопки для первых трех рядов
                        commitButtonAction(buttonNumber);
                    } else if (row == 3 && col < greenCols) {
                        int buttonNumber = 21 + col1; // Номера кнопок для 4-го ряда
                        commitButtonAction(buttonNumber);
                    } else if (row >= 4 && col < cols) {
                        int buttonNumber = (row - 4) * cols + col + 31; // Номер кнопки для последних трех рядов
                        commitButtonAction(buttonNumber);
                    }
                }
                return true;
                // Возвращаем true, если обработали нажатие кнопки клавиатуры
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // Отпускаем кнопку Backspace, если она была нажата
                if (isBackspacePressed) {
                    stopBackspace();
                    isBackspacePressed = false;
                }
            }

            return super.onTouchEvent(event); // Обрабатываем остальные события
        }



        // Обработка действия кнопки
        private void commitButtonAction(int buttonNumber) {
            InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection != null) {
                String text = "";
                switch (buttonNumber) {
                    case 8: // Shift
                        toggleShift();
                        return; // Без текста
                    case 12: // Backspace
                        isBackspacePressed = true; // Кнопка Backspace нажата
                        startBackspace(inputConnection);
                        return;
                    case 21: text = "0"; break;
                    case 22: text = "1"; break;
                    case 23: text = "2"; break;
                    case 24: text = "3"; break;
                    case 25: text = "4"; break;
                    case 26: text = "5"; break;
                    case 27: text = "6"; break;
                    case 28: text = "7"; break;
                    case 29: text = "8"; break;
                    case 30: text = "9"; break;
                    case 39: // Space
                        currentText.append(" ");
                        inputConnection.commitText(" ", 1);
                        break;
                    case 43: inputConnection.commitText("\n", 1);
                        return; // Enter
                    case 34: toggleLanguage(); return; // Переключение языка
                    case 41: toggleSymbolMode(); return;
                    case 48: calculateExpression(); break; // Переключение режима символов// Переключение режима символов
                    default:
                        text = getButtonText(buttonNumber);
                }
                currentText.append(text); // Добавляем символ в буфер
                inputConnection.commitText(text, 1);
            }
        }

        // Переключение режима символов
        private void toggleSymbolMode() {
            isSymbolMode = !isSymbolMode; // Переключаем режим
            invalidate(); // Перерисовываем клавиатуру
        }

        // Переключение языка
        private void toggleLanguage() {
            switch (currentLanguage) {
                case "EN":
                    currentLanguage = "RU";
                    break;
                case "RU":
                    currentLanguage = "UA";
                    break;
                case "UA":
                default:
                    currentLanguage = "EN";
                    break;
            }
            invalidate(); // Перерисовать клавиатуру
        }

        // Переключение регистра (Shift)
        private void toggleShift() {
            isShifted = !isShifted; // Переключаем регистр
            invalidate(); // Обновляем клавиатуру
        }

        private String getButtonText(int buttonNumber) {
            return getButtonLabel(buttonNumber);
        }
        // Получение текста для кнопки
    }


    // Метод для запуска "залипания" backspace
    private void startBackspace(InputConnection inputConnection) {
        backspaceRunnable = () -> {
            if (isBackspacePressed) { // Проверка, нажата ли кнопка
                inputConnection.deleteSurroundingText(1, 0);
                handler.postDelayed(backspaceRunnable, 100);
            }
        };
        handler.post(backspaceRunnable);
    }

    // Метод для остановки "залипания" backspace
    private void stopBackspace() {
        handler.removeCallbacks(backspaceRunnable);
    }

    // Метод для вычисления математического выражения
    private void calculateExpression() {
        String expression = currentText.toString();

        try {
            Expression exp = new ExpressionBuilder(expression).build();
            double result = exp.evaluate();
            InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection != null) {
                inputConnection.commitText(" = " + result + "\n", 1); // добавляем результат и новую строку
            }
            currentText.setLength(0); // Очистка после вычисления
        } catch (Exception e) {
            Toast.makeText(MyKeyboardService.this, "Ошибка в выражении", Toast.LENGTH_SHORT).show();
        }
    }

}