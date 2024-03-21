package com.loopy.loopy.plugins.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawRequest {
    String model;
    Input input;
    Parameters parameters;

    public DrawRequest(String prompt){
        model  = "wanx-v1";
        input = new Input(prompt);
        parameters = new Parameters("<sketch>", "1024*1024", 4, 42);
    }
    class Input {
        public String prompt;
        Input(String prompt){
            this.prompt = prompt;
        }
    }
    class Parameters {
        public String style;
        public String size;
        public int n;
        public int seed;

        public Parameters(String style, String size, int n, int seed) {
            this.style = style;
            this.size = size;
            this.n = n;
            this.seed = seed;
        }
    }
}
