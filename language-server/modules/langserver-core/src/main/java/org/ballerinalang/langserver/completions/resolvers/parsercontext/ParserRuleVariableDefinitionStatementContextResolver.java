/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.consts.ItemResolverConstants;
import org.ballerinalang.langserver.completions.consts.Priority;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.filters.ConnectorInitExpressionItemFilter;
import org.ballerinalang.langserver.completions.util.filters.PackageActionFunctionAndTypesFilter;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser rule based variable definition statement context resolver.
 */
public class ParserRuleVariableDefinitionStatementContextResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        PackageActionFunctionAndTypesFilter actionFunctionTypeFilter = new PackageActionFunctionAndTypesFilter();
        ConnectorInitExpressionItemFilter connectorInitItemFilter = new ConnectorInitExpressionItemFilter();

        // Here we specifically need to check whether the statement is function invocation,
        // action invocation or worker invocation
        if (isInvocationOrFieldAccess(completionContext)) {
            ArrayList<SymbolInfo> actionAndFunctions = new ArrayList<>();
            actionAndFunctions.addAll(actionFunctionTypeFilter.filterItems(completionContext));
            this.populateCompletionItemList(actionAndFunctions, completionItems);
            return completionItems;
        } else {
            // Fill completions if user is writing a connector init
            List<SymbolInfo> filteredConnectorInitSuggestions = connectorInitItemFilter.filterItems(completionContext);
            if (!filteredConnectorInitSuggestions.isEmpty()) {
                populateCompletionItemList(filteredConnectorInitSuggestions, completionItems);
                return completionItems;
            }

            // Add the create keyword
            CompletionItem createKeyword = new CompletionItem();
            createKeyword.setInsertText("create ");
            createKeyword.setLabel("create");
            createKeyword.setDetail(ItemResolverConstants.KEYWORD_TYPE);
            createKeyword.setSortText(Priority.PRIORITY7.name());

            List<SymbolInfo> filteredList = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                    .stream()
                    .filter(symbolInfo -> {
                        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                        SymbolKind symbolKind = bSymbol.kind;
                        
                        // Here we return false if the BType is not either a package symbol or ENUM
                        return !((bSymbol instanceof BTypeSymbol) && !(bSymbol instanceof BPackageSymbol
                                || SymbolKind.ENUM.equals(symbolKind)));
                    })
                    .collect(Collectors.toList());
            populateCompletionItemList(filteredList, completionItems);
            completionItems.add(createKeyword);
            return completionItems;
        }
    }
}
