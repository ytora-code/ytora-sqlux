package xyz.ytora.sqlux.sql.func;

import xyz.ytora.sqlux.sql.func.support.Abs;
import xyz.ytora.sqlux.sql.func.support.Alias;
import xyz.ytora.sqlux.sql.func.support.Avg;
import xyz.ytora.sqlux.sql.func.support.Cast;
import xyz.ytora.sqlux.sql.func.support.Coalesce;
import xyz.ytora.sqlux.sql.func.support.Concat;
import xyz.ytora.sqlux.sql.func.support.Count;
import xyz.ytora.sqlux.sql.func.support.CurrentTimestamp;
import xyz.ytora.sqlux.sql.func.support.Floor;
import xyz.ytora.sqlux.sql.func.support.Length;
import xyz.ytora.sqlux.sql.func.support.Lower;
import xyz.ytora.sqlux.sql.func.support.LTrim;
import xyz.ytora.sqlux.sql.func.support.Max;
import xyz.ytora.sqlux.sql.func.support.Min;
import xyz.ytora.sqlux.sql.func.support.NullIf;
import xyz.ytora.sqlux.sql.func.support.Power;
import xyz.ytora.sqlux.sql.func.support.Replace;
import xyz.ytora.sqlux.sql.func.support.Round;
import xyz.ytora.sqlux.sql.func.support.RTrim;
import xyz.ytora.sqlux.sql.func.support.Sqrt;
import xyz.ytora.sqlux.sql.func.support.Sum;
import xyz.ytora.sqlux.sql.func.support.Trim;
import xyz.ytora.sqlux.sql.func.support.Upper;

/**
 * 集合所有内置 SQL 函数的静态入口。
 *
 * <p>可通过静态导入后以贴近 SQL 的方式直接调用，例如 {@code abs(...)}、
 * {@code count()}、{@code currentTimestamp()}。</p>
 *
 * @author ytora
 * @since 1.0
 */
@SuppressWarnings("overloads")
public final class SqlFuncAggregation {

    private SqlFuncAggregation() {
    }

    public static <T, R> Abs abs(ColFunction<T, R> column) {
        return Abs.of(column);
    }

    public static Abs abs(SqlExpression expression) {
        return Abs.of(expression);
    }

    public static Abs abs(Object value) {
        return Abs.of(value);
    }

    public static <T, R> Alias alias(ColFunction<T, R> column) {
        return Alias.of(column);
    }

    public static Alias alias(SqlExpression expression) {
        return Alias.of(expression);
    }

    public static <T, R> Avg avg(ColFunction<T, R> column) {
        return Avg.of(column);
    }

    public static Avg avg(SqlExpression expression) {
        return Avg.of(expression);
    }

    public static <T, R> Cast cast(ColFunction<T, R> column, String targetType) {
        return Cast.of(column, targetType);
    }

    public static Cast cast(SqlExpression expression, String targetType) {
        return Cast.of(expression, targetType);
    }

    public static Cast cast(Object value, String targetType) {
        return Cast.of(value, targetType);
    }

    public static Coalesce coalesce(Object... arguments) {
        return Coalesce.of(arguments);
    }

    public static <T, R> Coalesce coalesce(ColFunction<T, R> first, Object... remaining) {
        return Coalesce.of(first, remaining);
    }

    public static Coalesce coalesce(SqlExpression first, Object... remaining) {
        return Coalesce.of(first, remaining);
    }

    public static Concat concat(Object... arguments) {
        return Concat.of(arguments);
    }

    public static <T, R> Concat concat(ColFunction<T, R> first, Object... remaining) {
        return Concat.of(first, remaining);
    }

    public static Count count() {
        return Count.of();
    }

    public static <T, R> Count count(ColFunction<T, R> column) {
        return Count.of(column);
    }

    public static Count count(SqlExpression expression) {
        return Count.of(expression);
    }

    public static CurrentTimestamp currentTimestamp() {
        return CurrentTimestamp.of();
    }

    public static <T, R> Floor floor(ColFunction<T, R> column) {
        return Floor.of(column);
    }

    public static Floor floor(SqlExpression expression) {
        return Floor.of(expression);
    }

    public static Floor floor(Object value) {
        return Floor.of(value);
    }

    public static <T, R> Length length(ColFunction<T, R> column) {
        return Length.of(column);
    }

    public static Length length(SqlExpression expression) {
        return Length.of(expression);
    }

    public static <T, R> Lower lower(ColFunction<T, R> column) {
        return Lower.of(column);
    }

    public static Lower lower(SqlExpression expression) {
        return Lower.of(expression);
    }

    public static <T, R> LTrim ltrim(ColFunction<T, R> column) {
        return LTrim.of(column);
    }

    public static LTrim ltrim(SqlExpression expression) {
        return LTrim.of(expression);
    }

    public static <T, R> Max max(ColFunction<T, R> column) {
        return Max.of(column);
    }

    public static Max max(SqlExpression expression) {
        return Max.of(expression);
    }

    public static <T, R> Min min(ColFunction<T, R> column) {
        return Min.of(column);
    }

    public static Min min(SqlExpression expression) {
        return Min.of(expression);
    }

    public static NullIf nullIf(Object first, Object second) {
        return NullIf.of(first, second);
    }

    public static <T, R> NullIf nullIf(ColFunction<T, R> first, Object second) {
        return NullIf.of(first, second);
    }

    public static NullIf nullIf(SqlExpression first, Object second) {
        return NullIf.of(first, second);
    }

    public static <T, R> Power power(ColFunction<T, R> column, Object exponent) {
        return Power.of(column, exponent);
    }

    public static Power power(SqlExpression expression, Object exponent) {
        return Power.of(expression, exponent);
    }

    public static Power power(Object value, Object exponent) {
        return Power.of(value, exponent);
    }

    public static Replace replace(Object source, Object search, Object replacement) {
        return Replace.of(source, search, replacement);
    }

    public static <T, R> Replace replace(ColFunction<T, R> source, Object search, Object replacement) {
        return Replace.of(source, search, replacement);
    }

    public static Replace replace(SqlExpression source, Object search, Object replacement) {
        return Replace.of(source, search, replacement);
    }

    public static <T, R> Round round(ColFunction<T, R> column) {
        return Round.of(column);
    }

    public static <T, R> Round round(ColFunction<T, R> column, int scale) {
        return Round.of(column, scale);
    }

    public static Round round(SqlExpression expression) {
        return Round.of(expression);
    }

    public static Round round(SqlExpression expression, int scale) {
        return Round.of(expression, scale);
    }

    public static Round round(Object value, int scale) {
        return Round.of(value, scale);
    }

    public static <T, R> RTrim rtrim(ColFunction<T, R> column) {
        return RTrim.of(column);
    }

    public static RTrim rtrim(SqlExpression expression) {
        return RTrim.of(expression);
    }

    public static <T, R> Sqrt sqrt(ColFunction<T, R> column) {
        return Sqrt.of(column);
    }

    public static Sqrt sqrt(SqlExpression expression) {
        return Sqrt.of(expression);
    }

    public static Sqrt sqrt(Object value) {
        return Sqrt.of(value);
    }

    public static <T, R> Sum sum(ColFunction<T, R> column) {
        return Sum.of(column);
    }

    public static Sum sum(SqlExpression expression) {
        return Sum.of(expression);
    }

    public static <T, R> Trim trim(ColFunction<T, R> column) {
        return Trim.of(column);
    }

    public static Trim trim(SqlExpression expression) {
        return Trim.of(expression);
    }

    public static <T, R> Upper upper(ColFunction<T, R> column) {
        return Upper.of(column);
    }

    public static Upper upper(SqlExpression expression) {
        return Upper.of(expression);
    }
}
