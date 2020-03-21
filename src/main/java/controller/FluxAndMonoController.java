package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Controller
public class FluxAndMonoController {

    @GetMapping("")
    public Flux<Integer> returnFlux(){
        System.out.println("main class2");
        return Flux.just(1,2,3,4).log();
    }

//    @RequestMapping("")
//    public String huuhu()
//    {
//        System.out.println("main class3");
//        return "hello";
//    }
}
