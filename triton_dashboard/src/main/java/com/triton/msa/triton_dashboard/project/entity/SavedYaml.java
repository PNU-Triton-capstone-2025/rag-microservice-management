package com.triton.msa.triton_dashboard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedYaml {

    private String fileName;
    private String yamlContent;
}
