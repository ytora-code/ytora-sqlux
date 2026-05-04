package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

import java.time.LocalDate;

/**
 * 内置类型：日期范围。
 *
 * <p>默认使用左闭右开区间 {@code [start,end)} 表达业务上的开始日期和结束日期。写入数据库时输出
 * PostgreSQL range 兼容的文本形式；不支持原生 range 的数据库可以用字符串字段保存同一格式。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class DateRange implements SqlReader, SqlWriter {

    private LocalDate start;

    private LocalDate end;

    private boolean startInclusive = true;

    private boolean endInclusive;

    /**
     * 创建空日期范围。
     */
    public DateRange() {
    }

    /**
     * 创建左闭右开的日期范围。
     *
     * @param start 开始日期；可以为 {@code null} 表示无下界
     * @param end 结束日期；可以为 {@code null} 表示无上界
     */
    public DateRange(LocalDate start, LocalDate end) {
        this(start, end, true, false);
    }

    /**
     * 创建日期范围。
     *
     * @param start 开始日期；可以为 {@code null} 表示无下界
     * @param end 结束日期；可以为 {@code null} 表示无上界
     * @param startInclusive 开始边界是否包含
     * @param endInclusive 结束边界是否包含
     */
    public DateRange(LocalDate start, LocalDate end, boolean startInclusive, boolean endInclusive) {
        this.start = start;
        this.end = end;
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
    }

    /**
     * 创建左闭右开的日期范围。
     *
     * @param start 开始日期
     * @param end 结束日期
     * @return 日期范围
     */
    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(start, end);
    }

    /**
     * 解析日期范围字符串。
     *
     * @param value 日期范围字符串，例如 {@code [2026-01-01,2026-02-01)}
     * @return 日期范围
     */
    public static DateRange parse(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.length() < 2) {
            throw new IllegalArgumentException("日期范围格式不正确: " + value);
        }
        boolean startInclusive = parseStartInclusive(text.charAt(0), value);
        boolean endInclusive = parseEndInclusive(text.charAt(text.length() - 1), value);
        String body = text.substring(1, text.length() - 1);
        String[] parts = splitBounds(body, value);
        return new DateRange(parseDate(parts[0]), parseDate(parts[1]), startInclusive, endInclusive);
    }

    /**
     * 获取开始日期。
     *
     * @return 开始日期
     */
    public LocalDate getStart() {
        return start;
    }

    /**
     * 设置开始日期。
     *
     * @param start 开始日期
     */
    public void setStart(LocalDate start) {
        this.start = start;
    }

    /**
     * 获取结束日期。
     *
     * @return 结束日期
     */
    public LocalDate getEnd() {
        return end;
    }

    /**
     * 设置结束日期。
     *
     * @param end 结束日期
     */
    public void setEnd(LocalDate end) {
        this.end = end;
    }

    /**
     * 开始边界是否包含。
     *
     * @return 包含时返回 {@code true}
     */
    public boolean isStartInclusive() {
        return startInclusive;
    }

    /**
     * 结束边界是否包含。
     *
     * @return 包含时返回 {@code true}
     */
    public boolean isEndInclusive() {
        return endInclusive;
    }

    /**
     * 判断指定日期是否在范围内。
     *
     * @param value 日期；入参为 {@code null} 时返回 {@code false}
     * @return 在范围内时返回 {@code true}
     */
    public boolean contains(LocalDate value) {
        if (value == null) {
            return false;
        }
        if (start != null) {
            int result = value.compareTo(start);
            if (result < 0 || result == 0 && !startInclusive) {
                return false;
            }
        }
        if (end != null) {
            int result = value.compareTo(end);
            return result <= 0 && (result != 0 || endInclusive);
        }
        return true;
    }

    @Override
    public DateRange read(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof DateRange) {
            return (DateRange) value;
        }
        return parse(String.valueOf(value));
    }

    @Override
    public Object write() {
        return toString();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(startInclusive ? '[' : '(')
                .append(start == null ? "" : start)
                .append(',')
                .append(end == null ? "" : end)
                .append(endInclusive ? ']' : ')')
                .toString();
    }

    private static LocalDate parseDate(String value) {
        String text = unquote(value.trim());
        if (text.isEmpty()) {
            return null;
        }
        return LocalDate.parse(text);
    }

    private static String[] splitBounds(String body, String source) {
        int comma = body.indexOf(',');
        if (comma < 0 || body.indexOf(',', comma + 1) >= 0) {
            throw new IllegalArgumentException("日期范围格式不正确: " + source);
        }
        return new String[]{body.substring(0, comma), body.substring(comma + 1)};
    }

    private static boolean parseStartInclusive(char value, String source) {
        if (value == '[') {
            return true;
        }
        if (value == '(') {
            return false;
        }
        throw new IllegalArgumentException("日期范围起始边界格式不正确: " + source);
    }

    private static boolean parseEndInclusive(char value, String source) {
        if (value == ']') {
            return true;
        }
        if (value == ')') {
            return false;
        }
        throw new IllegalArgumentException("日期范围结束边界格式不正确: " + source);
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
