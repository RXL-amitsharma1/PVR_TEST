<g:if test="${reportFields}">
    <g:select id="allowedFields" name="allowedFields"
              from="${reportFields}"
              optionKey="id" optionValue="displayName"
              multiple="true">
    </g:select>
</g:if>