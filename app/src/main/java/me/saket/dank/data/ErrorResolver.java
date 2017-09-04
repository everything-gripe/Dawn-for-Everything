package me.saket.dank.data;

import android.support.annotation.Nullable;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.exceptions.UndeliverableException;
import me.saket.dank.R;
import retrofit2.HttpException;
import rx.exceptions.OnErrorThrowable;

/**
 * Resolves commonly encountered errors and and constructs a user understandable
 * title and message that can be shown by the View.
 */
public class ErrorResolver {

  public ResolvedError resolve(@Nullable Throwable error) {
    error = findActualCause(error);

    if (error instanceof SocketException || error instanceof SocketTimeoutException || error instanceof UnknownHostException) {
      return ResolvedError.create(
          ResolvedError.Type.NETWORK_ERROR,
          R.string.common_network_error_emoji,
          R.string.common_network_error_message
      );

    } else if (error instanceof HttpException && ((HttpException) error).code() == 503) {
      return ResolvedError.create(
          ResolvedError.Type.REDDIT_IS_DOWN,
          R.string.common_reddit_is_down_error_emoji,
          R.string.common_reddit_is_down_error_message
      );

    } else {
      return ResolvedError.create(
          ResolvedError.Type.UNKNOWN,
          R.string.common_unknown_error_emoji,
          R.string.common_unknown_error_message
      );
    }
  }

  public Throwable findActualCause(@Nullable Throwable error) {
    if (error instanceof io.reactivex.exceptions.CompositeException) {
      error = error.getCause();
    }
    if (error instanceof rx.exceptions.CompositeException) {
      error = error.getCause();
    }
    if (error instanceof UndeliverableException) {
      error = error.getCause();
    }
    if (error instanceof OnErrorThrowable) {
      error = error.getCause();
    }
    if (error instanceof RuntimeException && error.getCause() != null) {
      // Stupid JRAW wraps all HTTP exceptions with RuntimeException.
      error = error.getCause();
    }
    if (error instanceof IllegalStateException && error.getMessage() != null && error.getMessage().contains("Reached retry limit")) {
      error = error.getCause();
    }
    return error;
  }
}
