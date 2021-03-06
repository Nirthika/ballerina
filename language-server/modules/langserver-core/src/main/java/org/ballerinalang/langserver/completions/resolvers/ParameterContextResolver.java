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

package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.consts.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parameter context resolver for resolving the items of the parameter context.
 */
public class ParameterContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredSymbols = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                .stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                .collect(Collectors.toList());

        filteredSymbols.forEach(symbolInfo -> {
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(symbolInfo.getSymbolName());
            completionItem.setInsertText(symbolInfo.getSymbolName());
            completionItem.setDetail(ItemResolverConstants.B_TYPE);
            completionItems.add(completionItem);
        });

        this.populateBasicTypes(completionItems, completionContext.get(DocumentServiceKeys.SYMBOL_TABLE_KEY));

        return completionItems;
    }
}
