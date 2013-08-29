package de.dainel.cleanqualifiedtypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @author Michael Ernst
 */
public class QualifiedTypeDetector extends ASTVisitor
{
   private Map<String, ImportDeclaration>           imports              = new HashMap<String, ImportDeclaration>();
   private Map<String, QualifiedTypeBindingManager> bindingManagers      = new HashMap<String, QualifiedTypeBindingManager>();
   private Map<String, String>                      usedTypes            = new HashMap<String, String>();
   private List<String>                             compilationUnitTypes = new ArrayList<String>();
   private List<String>                             propertyNames        = new ArrayList<String>();

   private void handleName( Name name )
   {
      if( name.isSimpleName() )
      {
         usedTypes.put( name.getFullyQualifiedName(),
                        resolveFullyQualifiedNameFromImports( name.getFullyQualifiedName() ) );
      }
      if( name.isQualifiedName() )
      {
         String qualifiedName = name.getFullyQualifiedName();
         QualifiedTypeBindingManager qm = bindingManagers.get( qualifiedName );
         if( qm == null )
         {
            qm = new QualifiedTypeBindingManager();
            if( imports.containsKey( qualifiedName ) )
            {
               qm.setImportAlreadyExists( true );
            }
         }
         qm.addQualifiedName( (QualifiedName) name );
         bindingManagers.put( qualifiedName, qm );
      }
   }

   private String resolveFullyQualifiedNameFromImports( String name )
   {
      for( String key : imports.keySet() )
      {
         if( key.endsWith( "." + name ) )
         {
            return key;
         }
      }
      return "java.lang." + name;
   }

   public boolean visit( TypeDeclaration node )
   {
      SimpleName name = node.getName();
      StringBuilder qualifiedType = new StringBuilder( name.getFullyQualifiedName() );
      if(node.getParent() instanceof CompilationUnit) {
    	  CompilationUnit cu = (CompilationUnit) node.getParent();
    	  qualifiedType.insert( 0, cu.getPackage().getName().getFullyQualifiedName() + "." );
    	  compilationUnitTypes.add( qualifiedType.toString() );
      }
      return true;
   }

   @Override
   public boolean visit( SimpleType st )
   {
      Name name = st.getName();
      handleName( name );
      return true;
   }

   @Override
   public boolean visit( NormalAnnotation node )
   {
      Name name = node.getTypeName();
      handleName( name );
      return true;
   }

   @Override
   public boolean visit( MarkerAnnotation node )
   {
      Name name = node.getTypeName();
      handleName( name );
      return true;
   }

   @Override
   public boolean visit( ImportDeclaration importNode )
   {
      Name name = importNode.getName();
      String fullyQualifiedName = name.getFullyQualifiedName();
      imports.put( fullyQualifiedName, importNode );
      return false;
   }

   @Override
   public boolean visit( SingleMemberAnnotation node )
   {
      Name name = node.getTypeName();
      handleName( name );
      return true;
   }

   @Override
   public boolean visit( MethodInvocation node )
   {
      Expression expression = node.getExpression();
      if( expression instanceof QualifiedName )
      {
         handleName( ( (QualifiedName) expression ) );
      }
      return true;
   }

   @Override
   public boolean visit( FieldDeclaration node )
   {
      @SuppressWarnings( "unchecked" )
      List<VariableDeclarationFragment> fragments = node.fragments();
      for( VariableDeclarationFragment fragment : fragments )
      {
         SimpleName name = fragment.getName();
         propertyNames.add( name.getFullyQualifiedName() );
      }
      return true;
   }

   @Override
   public boolean visit( MemberValuePair node )
   {
      Expression value = node.getValue();
      if( value instanceof QualifiedName )
      {
         QualifiedName name = ( (QualifiedName) value );
         Name qualifier = name.getQualifier();
         if( qualifier instanceof QualifiedName )
         {
            handleName( qualifier );
         }
      }
      return true;
   }

   private String getUnqualifiedName( String fullyQualifiedName )
   {
      int lastDot = fullyQualifiedName.lastIndexOf( '.' );
      if( lastDot == -1 )
      {
         return fullyQualifiedName;
      }
      else
      {
         return fullyQualifiedName.substring( ++lastDot );
      }
   }

   public List<QualifiedTypeBindingManager> getTypeManagers()
   {
      List<QualifiedTypeBindingManager> result = new ArrayList<QualifiedTypeBindingManager>();
      List<String> unqualifiedNamesInResult = new ArrayList<String>();
      for( Entry<String, QualifiedTypeBindingManager> entry : bindingManagers.entrySet() )
      {
         String unqualifiedName = getUnqualifiedName( entry.getKey() );
         if( propertyNames.contains( unqualifiedName ) || unqualifiedNamesInResult.contains(unqualifiedName))
         {
            continue;
         }
         boolean found = false;
         for( String compType : compilationUnitTypes )
         {
            String ownCompilationUnits = getUnqualifiedName( compType );
            if( ownCompilationUnits.equals( unqualifiedName ) && !compType.equals( entry.getKey() ) )
            {
               found = true;
               break;
            }
         }
         if( found )
         {
            continue;
         }
         if( compilationUnitTypes.contains( unqualifiedName ) )
         {
            continue;
         }
         String alreadyUsed = usedTypes.get( unqualifiedName );
         if( alreadyUsed != null && alreadyUsed.equals( entry.getKey() ) )
         {
            result.add( entry.getValue() );
            unqualifiedNamesInResult.add(unqualifiedName);
         }
         else if( alreadyUsed != null && !alreadyUsed.equals( entry.getKey() ) )
         {
            continue;
         }
         else
         {
            result.add( entry.getValue() );
            unqualifiedNamesInResult.add(unqualifiedName);
         }
      }
      return result;
   }
}
