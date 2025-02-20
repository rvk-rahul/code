package com.emredeniz.demo.model;

import java.util.Set;

public record HttpResponse (String message, Set<String> blacklistedIbans, int status) {}
