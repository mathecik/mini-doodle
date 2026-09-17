   package com.gulsah.mini_doodle.exception;

   public class BadRequestException extends RuntimeException {
       public BadRequestException(String message) {
           super(message);
       }
   }