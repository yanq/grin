package grace.controller

import grace.route.Route

abstract class ControllerScript extends Script{
    List<Route> routes = []

    ControllerScript(Binding binding){
        super(binding)
    }

    def get(String path, Closure closure) {
        routes.add(new Route(path: path, closure: closure))
    }

    def s(String m){
        println m
    }
}
