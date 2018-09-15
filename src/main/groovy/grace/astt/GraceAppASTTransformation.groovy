package grace.astt

import grace.app.GraceApp
import grace.controller.ControllerScript
import groovy.transform.CompileStatic
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
        println("ASTTransformation @ ${source.name} -- $nodes")
        if (GraceApp.isController(source.name)){
            source.AST.classes[0].setSuperClass(new ClassNode(ControllerScript))
        }
    }
}
