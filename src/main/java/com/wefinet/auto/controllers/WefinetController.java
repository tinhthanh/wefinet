package com.wefinet.auto.controllers;

import com.wefinet.auto.entitys.WefinetModel;
import com.wefinet.auto.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class WefinetController {
    @Autowired
    private ProductService productService;
    @PostMapping("/save")
    public String save(@RequestBody WefinetModel p) throws ExecutionException, InterruptedException {
      return this.productService.saveDoc(p);
     }
    @PostMapping("/update")
    public String update(@RequestBody WefinetModel p) throws ExecutionException, InterruptedException {
        return this.productService.updateDoc(p);
    }
    @GetMapping("/find-by-id/{id}")
    public WefinetModel getById(@PathVariable String id) throws ExecutionException, InterruptedException {
        return this.productService.getDocId(id);
    }
}
