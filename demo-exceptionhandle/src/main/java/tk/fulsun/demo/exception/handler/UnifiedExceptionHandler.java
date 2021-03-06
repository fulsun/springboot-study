package tk.fulsun.demo.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import tk.fulsun.demo.exception.BaseException;
import tk.fulsun.demo.exception.BusinessException;
import tk.fulsun.demo.exception.constant.enums.ArgumentResponseEnum;
import tk.fulsun.demo.exception.constant.enums.CommonResponseEnum;
import tk.fulsun.demo.exception.constant.enums.ServletResponseEnum;
import tk.fulsun.demo.exception.i18n.UnifiedMessageSource;
import tk.fulsun.demo.exception.pojo.response.ErrorResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * @author fulsun
 * @date 7/1/2021
 */
@Slf4j
@ControllerAdvice
@ConditionalOnWebApplication
public class UnifiedExceptionHandler {
    /**
     * ????????????
     */
    private static final String ENV_PROD = "prod";

    /**
     * ????????????
     */
    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    private UnifiedMessageSource unifiedMessageSource;

    /**
     * ????????????
     *
     * @param e ??????
     * @return ????????????
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ErrorResponse handleBusinessException(BaseException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getResponseEnum().getCode(), getMessage(e));
    }

    /**
     * ???????????????
     *
     * @param e ??????
     * @return ????????????
     */
    @ExceptionHandler(value = BaseException.class)
    @ResponseBody
    public ErrorResponse handleBaseException(BaseException e) {
        log.error(e.getMessage(), e);

        return new ErrorResponse(e.getResponseEnum().getCode(), getMessage(e));
    }

    /**
     * Controller?????????????????????
     *
     * @param e ??????
     * @return ????????????
     */
    @ExceptionHandler({
            NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            // BindException.class,
            // MethodArgumentNotValidException.class
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            MissingServletRequestPartException.class,
            AsyncRequestTimeoutException.class
    })
    @ResponseBody
    public ErrorResponse handleServletException(Exception e) {
        log.error(e.getMessage(), e);
        int code = CommonResponseEnum.SERVER_ERROR.getCode();
        try {
            ServletResponseEnum servletExceptionEnum =
                    ServletResponseEnum.valueOf(e.getClass().getSimpleName());
            code = servletExceptionEnum.getCode();
        } catch (IllegalArgumentException e1) {
            log.error(
                    "class [{}] not defined in enum {}",
                    e.getClass().getName(),
                    ServletResponseEnum.class.getName());
        }

        if (ENV_PROD.equals(profile)) {
            // ??????????????????, ????????????????????????????????????????????????, ??????404.
            code = CommonResponseEnum.SERVER_ERROR.getCode();
            BaseException baseException = new BaseException(CommonResponseEnum.SERVER_ERROR);
            String message = getMessage(baseException);
            return new ErrorResponse(code, message);
        }

        return new ErrorResponse(code, e.getMessage());
    }

    /**
     * ??????????????????
     *
     * @param e ??????
     * @return ????????????
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ErrorResponse handleBindException(BindException e) {
        log.error("??????????????????", e);

        return wrapperBindingResult(e.getBindingResult());
    }

    /**
     * ????????????(Valid)??????????????????????????????????????????????????????????????????
     *
     * @param e ??????
     * @return ????????????
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public ErrorResponse handleValidException(MethodArgumentNotValidException e) {
        log.error("??????????????????", e);

        return wrapperBindingResult(e.getBindingResult());
    }

    /**
     * ????????????(??????)??????????????????????????????????????????????????????????????????
     *
     * @param e ??????
     * @return ????????????
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public ErrorResponse handleViolationException(ConstraintViolationException e) {
        log.error("??????????????????", e);
        StringBuilder msg = new StringBuilder();
        for (ConstraintViolation constraintViolation : e.getConstraintViolations()) {
            msg.append(constraintViolation.getMessage() == null ? "" : constraintViolation.getMessage());
        }
        return new ErrorResponse(ArgumentResponseEnum.VALID_ERROR.getCode(), msg.toString());
    }

    /**
     * ????????????????????????
     *
     * @param bindingResult ????????????
     * @return ????????????
     */
    private ErrorResponse wrapperBindingResult(BindingResult bindingResult) {
        StringBuilder msg = new StringBuilder();

        for (ObjectError error : bindingResult.getAllErrors()) {
            msg.append(", ");
            if (error instanceof FieldError) {
                msg.append(((FieldError) error).getField()).append(": ");
            }
            msg.append(error.getDefaultMessage() == null ? "" : error.getDefaultMessage());
        }

        return new ErrorResponse(ArgumentResponseEnum.VALID_ERROR.getCode(), msg.substring(2));
    }

    /**
     * ???????????????
     *
     * @param e ??????
     * @return ????????????
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ErrorResponse handleException(Exception e) {
        log.error(e.getMessage(), e);

        if (ENV_PROD.equals(profile)) {
            // ??????????????????, ????????????????????????????????????????????????, ???????????????????????????.
            int code = CommonResponseEnum.SERVER_ERROR.getCode();
            BaseException baseException = new BaseException(CommonResponseEnum.SERVER_ERROR);
            String message = getMessage(baseException);
            return new ErrorResponse(code, message);
        }

        return new ErrorResponse(CommonResponseEnum.SERVER_ERROR.getCode(), e.getMessage());
    }

    /**
     * ?????????????????????
     *
     * @param e ??????
     * @return
     */
    public String getMessage(BaseException e) {
        String code = "response." + e.getResponseEnum().toString();
        String message = unifiedMessageSource.getMessage(code, e.getArgs());

        if (message == null || message.isEmpty()) {
            return e.getMessage();
        }

        return message;
    }
}
