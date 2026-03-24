// package uk.ac.man.cs.eventlite.controllers;

// import org.springframework.hateoas.MediaTypes;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping(value = "/api", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
// public class HomeControllerApi {

//     private static final String HOME_MSG = "{ \"hi\": \"bye\" }";

//     @GetMapping
//     public ResponseEntity<?> getApiHome() {
//         return ResponseEntity.ok(HOME_MSG);
//     }

// }    