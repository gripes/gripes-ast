package net.sf.gripes.entity.transform;


import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Transient

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class EntityASTTransformation implements ASTTransformation {
	Logger logger = LoggerFactory.getLogger(EntityASTTransformation.class);
	
	@Override public void visit(ASTNode[] nodes, SourceUnit source) {
		if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
			throw new RuntimeException("Internal error: wrong types: ${node.class} / ${parent.class}");
		}
		
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

		FieldNode mappings = (((ClassNode) parent).getFields()).find{it.name=="mappings"}

		if(mappings) {
			def method, args
			AnnotationNode trans = new AnnotationNode(new ClassNode(Transient.class))
			mappings.addAnnotation(trans)
			
			mappings.initialValueExpression.code.statements.each {
				args = []
				method = it.expression.method.value
				it.expression.arguments.each {
					it.expressions.each {
						args.add it.variable
					}
				}
			}
			
			ClassNode parentNode = (ClassNode) parent
			AnnotationNode oneToMany = new AnnotationNode(new ClassNode(OneToMany.class))
			
			//Find the field on the parent ClassNode and attach OneToMany
			args.each {
				FieldNode fn = parentNode.getField(it)
				
				final ClassNode listCN = ClassHelper.LIST_TYPE.getPlainNodeReference(); 
				listCN.setUsingGenerics(true)
				listCN.setGenericsTypes(new GenericsType(fn.type))
				
				fn.setType(listCN)
				fn.addAnnotation(oneToMany)
			}
		}
		
		AnnotationNode annotation = new AnnotationNode(new ClassNode(javax.persistence.Entity.class))
		parent.addAnnotation(annotation)
	}
}