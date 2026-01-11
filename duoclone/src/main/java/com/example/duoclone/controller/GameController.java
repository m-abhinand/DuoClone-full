package com.example.duoclone.controller;

import com.example.duoclone.model.GameQuestion;
import com.example.duoclone.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/start/{category}")
    public List<GameQuestion> startGame(@PathVariable String category) {
        return gameService.startTest(category.toUpperCase());
    }
}
