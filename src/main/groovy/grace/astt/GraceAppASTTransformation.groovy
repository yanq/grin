package grace.astt

import grace.app.GraceApp
import grace.controller.ControllerScript
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class GraceAppASTTransformation implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        println("add supper class ControllerScript @ ${source?.name}")
        if (source.name.contains(GraceApp.APP_DIR) && source.name.contains(GraceApp.APP_CONTROLLERS)) {
            source.AST.classes[0].setSuperClass(new ClassNode(ControllerScript))
        }
    }
}
