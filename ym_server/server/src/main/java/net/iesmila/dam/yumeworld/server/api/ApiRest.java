package net.iesmila.dam.yumeworld.server.api;

import net.iesmila.dam.yumeworld.server.api.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class APIYumeworld {

    @GetMapping("/hello")
    public User hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new User(1,String.format("Hello %s!", name));
    }



//    @GetMapping("/hello")
//    public Missatge heldlo(@RequestParam(value = "name", defaultValue = "World") String name) {
//        // return String.format("Hello %s!", name);
//
//        return new Missatge(1, "Hola " + name, new Date());
//    }
//
//    @GetMapping("/sala")
//    public Missatge sala(@RequestParam(value = "name", defaultValue = "World") String name) {
//        // return String.format("Hello %s!", name);
//
//        return new Missatge(1, "Hola " + name, new Date());
//    }
}
