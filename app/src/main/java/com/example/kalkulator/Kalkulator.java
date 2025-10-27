package com.example.kalkulator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Kalkulator dengan dukungan modulo (%) sebagai sisa bagi.
 * - % integer: jika kedua operand bilangan bulat -> pakai integer modulo (long), aman dari floating quirks.
 * - % floating: jika salah satu operand pecahan -> pakai Java remainder (a % b).
 * - Divide/Modulo by zero -> "Error" (tanpa crash).
 * - State machine: INPUT_LEFT, INPUT_RIGHT, SHOW_RESULT.
 * - Chain setelah "=" dan repeat "=" didukung.
 * - Operator berganti tanpa duplikasi.
 * - Backspace cerdas; titik & leading zero aman.
 * - Hasil: integer tanpa .0, pecahan tetap desimal.
 */
public class Kalkulator extends AppCompatActivity {

    TextView txthasil, txtangka;

    // ------------------ Engine (State Machine) ------------------
    private static class Engine {
        enum Phase { INPUT_LEFT, INPUT_RIGHT, SHOW_RESULT } // enum adalah

        private StringBuilder left = new StringBuilder();   // teks operand kiri
        private StringBuilder right = new StringBuilder();  // teks operand kanan
        private String op = "";                             // "+", "-", "*", "/", "%"
        private Double lastResult = null;                   // hasil terakhir
        private Phase phase = Phase.INPUT_LEFT;

        // repeat "="
        private String lastOpRepeat = "";
        private Double lastBRepeat = null;

        // ---------- Utils ----------
        private static boolean isWhole(double v) {
            return v == Math.rint(v);
        }

        private static String stripZeros(String s) {
            if (!s.contains(".")) return s;
            while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
            if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
            return s;
        }

        private static String fmtDouble(double v) {
            if (Double.isNaN(v) || Double.isInfinite(v)) return "Error";
            return isWhole(v) ? String.valueOf((long)Math.round(v)) : stripZeros(Double.toString(v));
        }

        private static double parseOrZero(String s) {
            if (s == null || s.isEmpty() || s.equals("-")) return 0.0;
            return Double.parseDouble(s);
        }

        private static String normalizedAppend(String current, String token) {
            // Aturan input angka & titik:
            // - Titik: hanya satu kali per bilangan; kalau awal -> "0."
            // - Leading zero: "0" diikuti digit -> ganti "0" (kecuali titik)
            if (".".equals(token)) {
                if (current.isEmpty()) return "0.";
                if (current.contains(".")) return current;
                return current + ".";
            }
            if (token.matches("\\d")) {
                if (current.equals("0")) return token; // cegah "01"
                return current + token;
            }
            return current;
        }

        private static String toggleSign(String s) {
            if (s.isEmpty()) return s;
            if (s.equals("-")) return "";
            return s.startsWith("-") ? s.substring(1) : "-" + s;
        }

        // ---------- Input angka & titik ----------
        void inputDigit(String d) {
            switch (phase) {
                case SHOW_RESULT:
                    // mulai baru
                    clearAll();
                    // fallthrough
                case INPUT_LEFT: {
                    String next = normalizedAppend(left.toString(), d);
                    left.setLength(0);
                    left.append(next);
                    break;
                }
                case INPUT_RIGHT: {
                    String next = normalizedAppend(right.toString(), d);
                    right.setLength(0);
                    right.append(next);
                    break;
                }
            }
        }

        void inputDot() { inputDigit("."); }

        // ---------- Operator ----------
        void setOperator(String newOp) {
            if (!(newOp.equals("+") || newOp.equals("-") || newOp.equals("*") || newOp.equals("/") || newOp.equals("%")))
                return;

            switch (phase) {
                case SHOW_RESULT:
                    // lanjut dari hasil terakhir sebagai left
                    left.setLength(0);
                    left.append(lastResult == null ? "0" : fmtDouble(lastResult));
                    right.setLength(0);
                    op = newOp;
                    phase = Phase.INPUT_RIGHT;
                    break;

                case INPUT_LEFT:
                    if (left.length() == 0) left.append("0");
                    op = newOp;
                    phase = Phase.INPUT_RIGHT;
                    break;

                case INPUT_RIGHT:
                    if (right.length() == 0) {
                        // ganti operator saja
                        op = newOp;
                        return;
                    }
                    // sudah ada right -> hitung dulu, lalu set operator baru
                    computeOnce(false);
                    left.setLength(0);
                    left.append(fmtDouble(lastResult));
                    right.setLength(0);
                    op = newOp;
                    phase = Phase.INPUT_RIGHT;
                    break;
            }
        }

        // ---------- Sama dengan ----------
        void equalsPress() {
            switch (phase) {
                case INPUT_LEFT:
                    // "=" tanpa operator -> hasil = left
                    lastResult = parseOrZero(left.toString());
                    phase = Phase.SHOW_RESULT;
                    break;

                case INPUT_RIGHT:
                    if (op.isEmpty()) {
                        lastResult = parseOrZero(left.toString());
                        phase = Phase.SHOW_RESULT;
                        break;
                    }
                    computeOnce(true); // simpan pasangan untuk repeat "="
                    phase = Phase.SHOW_RESULT;
                    break;

                case SHOW_RESULT:
                    // repeat "=" bila ada pasangan terakhir
                    if (!lastOpRepeat.isEmpty() && lastBRepeat != null) {
                        double a = (lastResult == null) ? 0 : lastResult;
                        lastResult = safeApply(a, lastBRepeat, lastOpRepeat);
                    }
                    break;
            }
        }

        // ---------- Backspace & +/- ----------
        void backspace() {
            switch (phase) {
                case SHOW_RESULT: {
                    // kembali ke edit left = lastResult
                    String prev = fmtDouble(lastResult == null ? 0 : lastResult);
                    clearAll();
                    left.append(prev);
                    phase = Phase.INPUT_LEFT;
                    if (left.length() > 0) left.deleteCharAt(left.length() - 1);
                    break;
                }
                case INPUT_LEFT:
                    if (left.length() > 0) left.deleteCharAt(left.length() - 1);
                    break;

                case INPUT_RIGHT:
                    if (right.length() > 0) {
                        right.deleteCharAt(right.length() - 1);
                    } else if (!op.isEmpty()) {
                        op = "";
                        phase = Phase.INPUT_LEFT;
                    }
                    break;
            }
        }

        void togglePlusMinus() {
            switch (phase) {
                case SHOW_RESULT: {
                    String prev = fmtDouble(lastResult == null ? 0 : lastResult);
                    clearAll();
                    left.append(prev);
                    phase = Phase.INPUT_LEFT;
                    // fallthrough
                }
                case INPUT_LEFT: {
                    String toggled = toggleSign(left.toString());
                    left.setLength(0);
                    left.append(toggled);
                    break;
                }
                case INPUT_RIGHT: {
                    String toggled = toggleSign(right.toString());
                    right.setLength(0);
                    right.append(toggled);
                    break;
                }
            }
        }

        // ---------- Hitung sekali ----------
        private void computeOnce(boolean setRepeat) {
            double a = parseOrZero(left.toString());
            double b = parseOrZero(right.toString());
            double res = safeApply(a, b, op);

            lastResult = res;
            if (setRepeat) {
                lastOpRepeat = op;
                lastBRepeat  = b;
            }
            // Setelah hitung, siapkan untuk chain
            op = "";
            right.setLength(0);
        }

        // ---------- Apply operator dengan proteksi (NaN/Inf) ----------
        private double safeApply(double a, double b, String operator) {
            double result;
            switch (operator) {
                case "+": result = a + b; break;
                case "-": result = a - b; break;
                case "*": result = a * b; break;
                case "/": result = (b == 0.0) ? Double.NaN : a / b; break;
                case "%":
                    if (b == 0.0) return Double.NaN; // modulo by zero -> Error
                    // Integer modulo jika keduanya bulat & muat di long:
                    if (isWhole(a) && isWhole(b)) {
                        long la = (long) Math.round(a);
                        long lb = (long) Math.round(b);
                        // hindari overflow ekstrem; Java long cukup besar untuk kalkulator biasa
                        result = (double) (la % lb);
                    } else {
                        // fallback: floating remainder Java
                        result = a % b;
                    }
                    break;
                default:  result = b;
            }
            // normalisasi -0.0 -> 0.0
            if (result == 0.0) result = 0.0;
            return result;
        }

        // ---------- Tampilan ----------
        String getExpression() {
            // contoh: "23 % 5" atau "23 *"
            switch (phase) {
                case INPUT_LEFT:
                case SHOW_RESULT: {
                    String L = left.length() == 0 ? "" : left.toString();
                    if (!op.isEmpty()) return (L + " " + op).trim();
                    return L.isEmpty() ? "0" : L;
                }
                case INPUT_RIGHT: {
                    String L = (left.length() == 0) ? "0" : left.toString();
                    String R = right.toString();
                    if (op.isEmpty()) return L;
                    return (L + " " + op + (R.isEmpty() ? "" : " " + R)).trim();
                }
            }
            return "0";
        }

        String getResult() {
            if (lastResult == null) return "0";
            if (Double.isNaN(lastResult) || Double.isInfinite(lastResult)) return "Error";
            return fmtDouble(lastResult);
        }

        // dipanggil UI setelah "=" jika ingin melanjutkan chain dari hasil
        void primeLeftWithResult() {
            if (lastResult == null) return;
            left.setLength(0);
            left.append(fmtDouble(lastResult));
        }

        void clearAll() {
            left.setLength(0);
            right.setLength(0);
            op = "";
            lastResult = null;
            lastOpRepeat = "";
            lastBRepeat = null;
            phase = Phase.INPUT_LEFT;
        }
    }
    // ------------------ End Engine ------------------

    private final Engine engine = new Engine();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.kalkulator);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txthasil = findViewById(R.id.txthasil);
        txtangka = findViewById(R.id.txtangka);

        refreshDisplay();
    }

    // ---------- Display helper ----------
    private void refreshDisplay() {
        txtangka.setText(engine.getExpression());
        txthasil.setText(engine.getResult());
    }

    private void pushDigit(String d) { engine.inputDigit(d); refreshDisplay(); }
    private void pushOp(String op)    { engine.setOperator(op); refreshDisplay(); }

    // ---------- Digit ----------
    public void nol(View v) { pushDigit("0"); }
    public void satu(View v) { pushDigit("1"); }
    public void dua(View v) { pushDigit("2"); }
    public void tiga(View v) { pushDigit("3"); }
    public void empat(View v) { pushDigit("4"); }
    public void lima(View v) { pushDigit("5"); }
    public void enam(View v) { pushDigit("6"); }
    public void tujuh(View v) { pushDigit("7"); }
    public void delapan(View v) { pushDigit("8"); }
    public void sembilan(View v) { pushDigit("9"); }

    // ---------- Dot ----------
    public void dot(View v) { engine.inputDot(); refreshDisplay(); }

    // ---------- Operators ----------
    public void tambah(View v) { pushOp("+"); }
    public void kurang(View v) { pushOp("-"); }
    public void kali(View v)   { pushOp("*"); }
    public void bagi(View v)   { pushOp("/"); }
    public void persen(View v) { pushOp("%"); } // MODULO

    // ---------- Sama Dengan ----------
    @SuppressLint("SetTextI18n")
    public void samadengan(View v) {
        engine.equalsPress();
        // Sesuai permintaan: baris ekspresi tidak menambahkan "= hasil"
        // Hasil hanya di txthasil. Siapkan left dengan hasil untuk chain berikutnya.
        engine.primeLeftWithResult();
        refreshDisplay();
    }

    // ---------- Lain-lain ----------
    public void clear(View v) {
        engine.clearAll();
        refreshDisplay();
    }

    public void del(View v) {
        engine.backspace();
        refreshDisplay();
    }

    public void plusminus(View v) {
        engine.togglePlusMinus();
        refreshDisplay();
    }
}
