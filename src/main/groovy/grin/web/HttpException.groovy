package grin.web
/**
 * 异常
 */
class HttpException extends Exception {
    int status
    String message

    HttpException(int status, String message = '') {
        this.status = status
        this.message = message
    }

    @Override
    String toString() {
        return "HttpException（status=$status,message=$message)"
    }
}
