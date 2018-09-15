package grace.controller

import grace.route.Route

class ControllerScript{
    List<Route> routes = []

    def get(String path, Closure closure) {
        routes.add(new Route(path: path, closure: closure))
    }
}
