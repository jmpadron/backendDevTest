package com.inditex.similar_products.adapter.in.web.controller;

import com.inditex.similar_products.domain.exception.ProductNotFoundException;
import com.inditex.similar_products.domain.exception.SimilarProductsRetrievalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleProductNotFoundException(ProductNotFoundException e) {
    var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Product Not Found");
    problem.setDetail(e.getMessage());
    return new ResponseEntity<>(problem, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(SimilarProductsRetrievalException.class)
  public ResponseEntity<ProblemDetail> handleSimilarProductsRetrievalException(
      SimilarProductsRetrievalException e) {
    var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Failed to retrieve similar products");
    problem.setDetail(e.getMessage());
    return new ResponseEntity<>(problem, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ProblemDetail> handleRuntimeException(RuntimeException e) {
    var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Internal Server Error");
    problem.setDetail("Something went wrong. Please try again later.");
    return new ResponseEntity<>(problem, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
