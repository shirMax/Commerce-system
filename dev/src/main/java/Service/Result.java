package Service;

import util.Enums.ErrorStatus;
import util.Exceptions.MarketException;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Result of a method call.
 * May carry value if GOOD result or error status and error message if BAD
 *
 * @param <T> Type of value for good result.
 */
public class Result<T> {
    private final Optional<T> value;

    private Result(T value){
        this.value = Optional.ofNullable(value);
    }

    private Result(){
        this.value = Optional.empty();
    }

    public boolean isOk(){
        return value.isPresent();
    }

    public boolean errorOccured(){
        return value.isEmpty();
    }

    /**
     * For results with no return type (void)
     */
    private static class VoidResult<T> extends Result<T> {
        private VoidResult() {
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean errorOccured() {
            return false;
        }
    }

    public T getValue() {
        return value.orElseThrow();
    }

    public ErrorStatus getStatus(){
        return ErrorStatus.NO_ERROR;
    }

    public String getErrorMessage(){
        throw new NoSuchElementException("No error");
    }

    /**
     * For BAD results
     */
    private static class Error<T> extends Result<T> {
        private final String errMsg;
        private final ErrorStatus status;
        private Error(ErrorStatus status, String errMsg) {
            this.status = status;
            this.errMsg = errMsg;
        }

        @Override
        public ErrorStatus getStatus() {
            return status;
        }

        @Override
        public String getErrorMessage() {
            return errMsg;
        }
    }

    //Factory for results:
    public static <U> VoidResult<U> makeGood(){
        return new VoidResult<>();
    }

    public static <U> Result<U> makeGood(U value){
        return new Result<>(value);
    }

    public static <U> Error<U> makeBad(ErrorStatus status, String errorMessage){
        return new Error<>(status, errorMessage);
    }

    public static <U> Error<U> makeBad(MarketException e){
        return makeBad(e.getStatus(), e.getMessage());
    }
}
