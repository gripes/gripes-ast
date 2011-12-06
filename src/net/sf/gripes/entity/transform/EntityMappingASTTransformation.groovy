package net.sf.gripes.entity.transform;


import java.lang.reflect.Modifier

import javax.persistence.OneToMany

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.hibernate.FetchMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class EntityMappingASTTransformation implements ASTTransformation {
	Logger logger = LoggerFactory.getLogger(EntityMappingASTTransformation.class);
	
	@Override public void visit(ASTNode[] nodes, SourceUnit source) {
		if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
			throw new RuntimeException("Internal error: wrong types: ${node.class} / ${parent.class}");
		}
		
		println source.name
		
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

		node.each{
			println it.classNode.getProperties()
		}
		
        FieldNode fNode = (FieldNode) parent;
        String fName = fNode.getName();
		println "Fname: " + fName
		
		parent.setDeclaringClass(new ClassNode(List.class))
		println parent.getDeclaringClass()
		
		ClassNode oneToMany = new ClassNode(OneToMany.class);
		
		PropertyNode fetchP = new PropertyNode("fetch", Modifier.PUBLIC, new ClassNode(FetchMode.class), new ClassNode(parent.getClass()), null, null, null);
		oneToMany.addProperty(fetchP);

		AnnotationNode annotation = new AnnotationNode(oneToMany);		
		parent.addAnnotation(annotation);
	}
}