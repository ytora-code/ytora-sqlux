package xyz.ytora.sqlux.orm.type;

import xyz.ytora.sqlux.rw.SqlReader;
import xyz.ytora.sqlux.rw.SqlWriter;

import java.time.LocalTime;

/**
 * 内置类型：时间范围。
 *
 * <p>默认使用左闭右开区间 {@code [start,end)} 表达一天内的开始时间和结束时间，例如营业时间、
 * 预约时间段和任务执行窗口。写入数据库时输出字符串形式，所有数据库都可用字符串字段保存。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class TimeRange implements SqlReader, SqlWriter {

    private LocalTime start;

    private LocalTime end;

    private boolean startInclusive = true;

    private boolean endInclusive;

    /**
     * 创建空时间范围。
     */
    public TimeRange() {
    }

    /**
     * 创建左闭右开的时间范围。
     *
     * @param start 开始时间；可以为 {@code null} 表示无下界
     * @param end 结束时间；可以为 {@code null} 表示无上界
     */
    public TimeRange(LocalTime start, LocalTime end) {
        this(start, end, true, false);
    }

    /**
     * 创建时间范围。
     *
     * @param start 开始时间；可以为 {@code null} 表示无下界
     * @param end 结束时间；可以为 {@code null} 表示无上界
     * @param startInclusive 开始边界是否包含
     * @param endInclusive 结束边界是否包含
     */
    public TimeRange(LocalTime start, LocalTime end, boolean startInclusive, boolean endInclusive) {
        this.start = start;
        this.end = end;
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
    }

    /**
     * 创建左闭右开的时间范围。
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return 时间范围
     */
    public static TimeRange of(LocalTime start, LocalTime end) {
        return new TimeRange(start, end);
    }

    /**
     * 解析时间范围字符串。
     *
     * @param value 时间范围字符串，例如 {@code [09:00,18:00)}
     * @return 时间范围
     */
    public static TimeRange parse(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.length() < 2) {
            throw new IllegalArgumentException("时间范围格式不正确: " + value);
        }
        boolean startInclusive = parseStartInclusive(text.charAt(0), value);
        boolean endInclusive = parseEndInclusive(text.charAt(text.length() - 1), value);
        String body = text.substring(1, text.length() - 1);
        String[] parts = splitBounds(body, value);
        return new TimeRange(parseTime(parts[0]), parseTime(parts[1]), startInclusive, endInclusive);
    }

    /**
     * 获取开始时间。
     *
     * @return 开始时间
     */
    public LocalTime getStart() {
        return start;
    }

    /**
     * 设置开始时间。
     *
     * @param start 开始时间
     */
    public void setStart(LocalTime start) {
        this.start = start;
    }

    /**
     * 获取结束时间。
     *
     * @return 结束时间
     */
    public LocalTime getEnd() {
        return end;
    }

    /**
     * 设置结束时间。
     *
     * @param end 结束时间
     */
    public void setEnd(LocalTime end) {
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
     * 判断指定时间是否在范围内。
     *
     * @param value 时间；入参为 {@code null} 时返回 {@code false}
     * @return 在范围内时返回 {@code true}
     */
    public boolean contains(LocalTime value) {
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
    public TimeRange read(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof TimeRange) {
            return (TimeRange) value;
        }
        return parse(String.valueOf(value));
    }

    @Override
    public Object write() {
        return toString();
    }

    @Override
    public String toString() {
        return String.valueOf(startInclusive ? '[' : '(') +
                (start == null ? "" : start) +
                ',' +
                (end == null ? "" : end) +
                (endInclusive ? ']' : ')');
    }

    private static LocalTime parseTime(String value) {
        String text = unquote(value.trim());
        if (text.isEmpty()) {
            return null;
        }
        return LocalTime.parse(stripFractionBeyondNanos(text));
    }

    private static String[] splitBounds(String body, String source) {
        int comma = body.indexOf(',');
        if (comma < 0 || body.indexOf(',', comma + 1) >= 0) {
            throw new IllegalArgumentException("时间范围格式不正确: " + source);
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
        throw new IllegalArgumentException("时间范围起始边界格式不正确: " + source);
    }

    private static boolean parseEndInclusive(char value, String source) {
        if (value == ']') {
            return true;
        }
        if (value == ')') {
            return false;
        }
        throw new IllegalArgumentException("时间范围结束边界格式不正确: " + source);
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String stripFractionBeyondNanos(String value) {
        int dot = value.indexOf('.');
        if (dot < 0) {
            return value;
        }
        int end = dot + 1;
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end - dot - 1 <= 9) {
            return value;
        }
        return value.substring(0, dot + 10) + value.substring(end);
    }
}
