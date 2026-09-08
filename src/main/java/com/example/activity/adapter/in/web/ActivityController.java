package com.example.activity.adapter.in.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.activity.adapter.in.web.docs.ActivityControllerDocs;
import com.example.activity.application.port.in.DescribeActivityUseCase;

@Validated
@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController implements ActivityControllerDocs {

    private final DescribeActivityUseCase useCase;

    public ActivityController(DescribeActivityUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @GetMapping("/users/{userId}")
    public ActivityDescriptionResponse describe(@PathVariable Long userId) {
        return new ActivityDescriptionResponse(useCase.describeFor(userId));
    }
}
