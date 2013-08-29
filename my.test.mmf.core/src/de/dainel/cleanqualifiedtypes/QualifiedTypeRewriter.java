package de.dainel.cleanqualifiedtypes;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import de.dainel.cleanqualifiedtypes.util.GroupComparator;

/**
 * @author Michael Ernst
 */
public class QualifiedTypeRewriter
{

   private class ImportComparator extends GroupComparator<ImportDeclaration>
   {

      public ImportComparator( Comparator<ImportDeclaration> first, Comparator<ImportDeclaration> second )
      {
         super( first, second );
      }
   }

   private class GroupingComparator implements Comparator<ImportDeclaration>
   {

      private final List<String> groups;

      public GroupingComparator( List<String> groups )
      {
         this.groups = groups;
      }

      @Override
      public int compare( ImportDeclaration o1, ImportDeclaration o2 )
      {
         if( groups == null || groups.isEmpty() )
         {
            return 0;
         }
         int gi1 = getGroupIndex( o1 );
         int gi2 = getGroupIndex( o2 );
         return new Integer( gi1 ).compareTo( gi2 );
      }

      private int getGroupIndex( ImportDeclaration declaration )
      {
         String fullyQualifiedName = declaration.getName().getFullyQualifiedName();
         int[] bestMatching = new int[] { -1, -1 };
         for( int i = 0; i < groups.size(); i++ )
         {
            String prefix = groups.get( i );
            if( prefix.length() > bestMatching[1] && fullyQualifiedName.startsWith( prefix ) )
            {
               bestMatching[0] = i;
               bestMatching[1] = prefix.length();
            }
         }

         if( bestMatching[0] != -1 )
         {
            return bestMatching[0];
         }

         return groups.size();
      }
   }

   private class NameComparator implements Comparator<ImportDeclaration>
   {

      @Override
      public int compare( ImportDeclaration o1, ImportDeclaration o2 )
      {
         return Collator.getInstance().compare( o1.getName().getFullyQualifiedName(),
                                                o2.getName().getFullyQualifiedName() );
      }

   }

   private final CompilationUnit                   unit;
   private final List<QualifiedTypeBindingManager> typeManagers;

   private boolean                                 organizeImports = false;
   private List<String>                            importGroups    = null;

   public QualifiedTypeRewriter( CompilationUnit unit, List<QualifiedTypeBindingManager> typeManagers )
   {
      this.unit = unit;
      this.typeManagers = typeManagers;
   }

   public boolean rewrite()
   {
      boolean changed = false;
      if( !typeManagers.isEmpty() )
      {
         unit.recordModifications(); // turn on recording
         modify();
         changed = true;
      }
      if( organizeImports )
      {
         @SuppressWarnings( "unchecked" )
         List<ImportDeclaration> imports = unit.imports();
         List<ImportDeclaration> sorted = new ArrayList<ImportDeclaration>( imports );
         Collections.sort( sorted, new ImportComparator( new GroupingComparator( importGroups ), new NameComparator() ) );
         if( !sorted.equals( imports ) )
         {
            if( !changed )
            {
               unit.recordModifications(); // turn on recording
            }
            imports.clear();
            imports.addAll( sorted );
            changed = true;
         }
      }
      return changed;
   }

   private void modify()
   {
      @SuppressWarnings( "unchecked" )
      List<ImportDeclaration> imports = unit.imports();
      for( QualifiedTypeBindingManager mg : typeManagers )
      {
         if( !mg.importAlreadyExists() )
         {
            String fullyQualifiedName = mg.getQualifiedNames().iterator().next().getFullyQualifiedName();
            if( !isFiltered( unit, fullyQualifiedName ) )
            {
               createImport( imports, fullyQualifiedName );
            }
         }
         for( QualifiedName qn : mg.getQualifiedNames() )
         {
            String unqualifiedName = getUnqualifiedName( qn.getFullyQualifiedName() );
            ASTNode parent = qn.getParent();
            SimpleName newSimpleName = unit.getAST().newSimpleName( unqualifiedName );
            if( parent instanceof NormalAnnotation )
            {
               ( (NormalAnnotation) parent ).setTypeName( newSimpleName );
            }
            else if( parent instanceof MarkerAnnotation )
            {
               ( (MarkerAnnotation) parent ).setTypeName( newSimpleName );
            }
            else if( parent instanceof SingleMemberAnnotation )
            {
               ( (SingleMemberAnnotation) parent ).setTypeName( newSimpleName );
            }
            else if( parent instanceof SimpleType )
            {
               ( (SimpleType) parent ).setName( newSimpleName );
            }
            else if( parent instanceof MethodInvocation )
            {
               ( (MethodInvocation) parent ).setExpression( newSimpleName );
            }
            else if( parent instanceof QualifiedName )
            {
               ( (QualifiedName) parent ).setQualifier( newSimpleName );
            }
         }
      }
   }

   private boolean isFiltered( CompilationUnit unit, String qualifiedName )
   {
      if( qualifiedName.startsWith( "java.lang" ) )
         return true;
      if( qualifiedName.indexOf( '.' ) == -1 )
      {
         return true;
      }
      if( unit.getPackage() != null )
      {
         String packageName = unit.getPackage().getName().getFullyQualifiedName();
         String givenPackage = qualifiedName.substring( 0, qualifiedName.lastIndexOf( '.' ) );
         if(packageName.equals( givenPackage )) {
            return true;
         }
      }
     
      return false;
   }

   private void createImport( List<ImportDeclaration> imports, String fullyQualifiedName )
   {
      ImportDeclaration newImport = unit.getAST().newImportDeclaration();
      newImport.setName( unit.getAST().newName( fullyQualifiedName ) );
      newImport.setOnDemand( false );
      imports.add( newImport );
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

   public void setOrganizeImports( boolean organizeImports )
   {
      this.organizeImports = organizeImports;
   }

   public boolean isOrganizeImports()
   {
      return organizeImports;
   }

   public void setImportGroups( List<String> importGroups )
   {
      this.importGroups = importGroups;
   }

   public List<String> getImportGroups()
   {
      return importGroups;
   }
}
