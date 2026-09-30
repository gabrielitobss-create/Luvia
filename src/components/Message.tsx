import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import type {CodeBlock} from '../services/AiService';

type Props = {role: 'user' | 'assistant'; text: string; codeBlocks?: CodeBlock[]; onCopy: (value: string) => void};
export function Message({role, text, codeBlocks = [], onCopy}: Props) {
  return <View style={[styles.row, role === 'user' ? styles.userRow : styles.assistantRow]}>
    <View style={[styles.message, role === 'user' ? styles.user : styles.assistant]}>
      {role === 'assistant' && <Text style={styles.label}>LUVIA</Text>}
      <Text style={[styles.text, role === 'user' && styles.userText]}>{text}</Text>
      {codeBlocks.map((block, index) => <View key={`${block.code}-${index}`} style={styles.code}>
        <View style={styles.codeTop}><Text style={styles.meta}>{block.scriptType} · {block.placement}</Text><Pressable onPress={() => onCopy(block.code)}><Text style={styles.copy}>Copiar</Text></Pressable></View>
        <Text selectable style={styles.codeText}>{block.code}</Text>
        {!!block.explanation && <Text style={styles.explanation}>{block.explanation}</Text>}
      </View>)}
    </View>
  </View>;
}
const styles = StyleSheet.create({row:{width:'100%',marginBottom:14},userRow:{alignItems:'flex-end'},assistantRow:{alignItems:'flex-start'},message:{maxWidth:'88%',padding:14,borderRadius:18},user:{backgroundColor:'#6877E8'},assistant:{backgroundColor:'#151A29'},label:{fontSize:10,fontWeight:'800',letterSpacing:1,color:'#AAB5FF',marginBottom:6},text:{fontSize:16,lineHeight:23,color:'#E8EBF5'},userText:{color:'#fff'},code:{marginTop:12,borderRadius:12,padding:12,backgroundColor:'#080B14'},codeTop:{flexDirection:'row',justifyContent:'space-between',gap:10,marginBottom:10},meta:{flex:1,color:'#8FDECF',fontSize:11,fontWeight:'700'},copy:{color:'#AAB5FF',fontWeight:'700'},codeText:{fontFamily:'monospace',fontSize:12,color:'#D6E2FF',lineHeight:18},explanation:{fontSize:13,lineHeight:19,color:'#BBC4D7',marginTop:10}});
